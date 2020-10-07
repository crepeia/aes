import pandas as pd
import random
import spacy
import sys
from rasa_nlu.training_data import load_data
from rasa_nlu.model import Trainer
from rasa_nlu import config
from rasa_nlu.model import Interpreter

#remover prints do tensorflow
import os
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3' 

import tensorflow.compat.v1 as tf
#remover prints do tensorflow
tf.compat.v1.logging.set_verbosity(tf.compat.v1.logging.ERROR)

tf.disable_v2_behavior()

def select_answer(intent):
    global df
    i = random.randint(0, len(df[intent]))
    answer = df.at[i, intent]
    print(answer + " (" + intent +")")

def predict_intent(pathModel, message):
    predicted_intents = []
    interpreter = Interpreter.load(pathModel)
    # print(interpreter.parse(text))
    # return interpreter.parse(text)
    sentence = ""
    #print ("Começa chat")
    #while sentence is not "quit":
    sentence = message
    sentence = str(sentence)
    # print(sentence)
    intent = interpreter.parse(sentence)
    intent = intent["intent"]
    intent = intent["name"]
    select_answer(intent)

        # predicted_intents.append(intent)
        # print(predicted_intents[0] + ": "+ select_answer(predicted_intents[0]))
        # predicted_intents = []

answers = str(sys.argv[1])
df = pd.read_csv(answers, sep = '\t')
#print(df)
#print(str(sys.argv[3]))
predict_intent(str(sys.argv[2]), str(sys.argv[3]))