"""
Converte as intents em csv para json. Treinamento do modelo do Rasa usa este json.
"""

import json
import pandas as pd

intents_csv = "intents.csv"
df = pd.read_csv(intents_csv, sep='\t')

try:
    parsed_json = (json.load(open("data.json")))
except FileNotFoundError:
    print ("Arquivo data.json não encontrado")


for i in range (0, len(df['sentence'])):
    texto = df.at[i, 'sentence']#+"\n"
    intent = df.at[i, 'intent']#+"\n"
    parsed_json['rasa_nlu_data']['common_examples'].append({
        'text': texto,
        'intent': intent,
        'entities': []
    })

    with open('data.json', 'w') as outfile:
        json.dump(parsed_json, outfile)