package aes.utility;

import aes.model.Chat;
import aes.model.Message;
import aes.model.User;
import java.util.List;
import java.util.Objects;

public final class ChatbotPolicy {
    private ChatbotPolicy() {}

    public static boolean canRequest(User consultant, Message patientMessage) {
        if (consultant == null || consultant.getId() <= 0 || consultant.getEmail() == null ||
                !consultant.isConsultant() || !consultant.isUse_chatbot() || patientMessage == null) return false;
        Chat chat = patientMessage.getChat();
        User patient = chat == null || chat.getId() == null ? null : chat.getUser();
        return patient != null && patient.getId() > 0 && patient.getRelatedConsultant() != null &&
                Objects.equals(patient.getRelatedConsultant().getId(), consultant.getId()) &&
                String.valueOf(patient.getId()).equals(patientMessage.getIdFrom());
    }

    // Input must be ordered oldest first and selected from this chat only.
    public static String buildContext(User consultant, Message patientMessage, List<Message> messages) {
        if (!canRequest(consultant, patientMessage) || messages.size() > 10) {
            throw new IllegalArgumentException("INVALID_CHATBOT_CONTEXT");
        }
        String patientId = String.valueOf(patientMessage.getChat().getUser().getId());
        String consultantId = String.valueOf(consultant.getId());
        StringBuilder context = new StringBuilder();
        for (Message message : messages) {
            if (message.getChat() == null || !Objects.equals(message.getChat().getId(), patientMessage.getChat().getId())) {
                throw new IllegalArgumentException("INVALID_CHATBOT_CONTEXT");
            }
            String role;
            if (patientId.equals(message.getIdFrom())) role = "paciente";
            else if (consultantId.equals(message.getIdFrom())) role = "consultor";
            else continue;
            if (message.getContent() == null) continue;
            String text = message.getContent().replace('\r', ' ').replace('\n', ' ').replace('\t', ' ').trim();
            if (text.isEmpty()) continue;
            if (context.length() > 0) context.append('\n');
            context.append(role).append(": ").append(text);
            if (context.length() > 16000) throw new IllegalArgumentException("CHATBOT_CONTEXT_TOO_LARGE");
        }
        if (context.length() == 0) throw new IllegalArgumentException("EMPTY_CHATBOT_CONTEXT");
        return context.toString();
    }
}
