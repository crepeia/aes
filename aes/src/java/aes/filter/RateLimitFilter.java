package aes.filter;

import io.github.bucket4j.*;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter; // Importante para NetBeans/Java EE
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// A anotação abaixo aplica o filtro para TODAS as rotas (/*)
@WebFilter(filterName = "RateLimitFilter", urlPatterns = {"/*"})
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
  
    private Bucket criarNovoBalde() {
        Bandwidth sustainedLimit = Bandwidth.classic(600, Refill.greedy(600, Duration.ofMinutes(1)));
//        Bandwidth burstLimit = Bandwidth.classic(150, Refill.greedy(150, Duration.ofSeconds(5)));

        return Bucket.builder()
                .addLimit(sustainedLimit)
                .build();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Método executado quando o servidor sobe. Pode deixar vazio.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String ipCliente = req.getRemoteAddr();
        Bucket bucket = cache.computeIfAbsent(ipCliente, k -> criarNovoBalde());

        if (bucket.tryConsume(1)) {
            // SUCESSO: Passa para o próximo passo (Controllers/REST)
            chain.doFilter(request, response);
        } else {
            // FALHA: Retorna erro 429
            res.setStatus(429);
            res.getWriter().write("Muitas requisicoes. Tente novamente mais tarde.");
        }
    }

    @Override
    public void destroy() {
        // Limpeza quando o servidor desliga
    }
}