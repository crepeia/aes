import spacy
from rasa_nlu.training_data import load_data
from rasa_nlu.model import Trainer
from rasa_nlu import config
from rasa_nlu.model import Interpreter
import tensorflow.compat.v1 as tf
tf.disable_v2_behavior()

def train_bot(data_json, config_file, model_dir):
    training_data = load_data(data_json)
    trainer = Trainer(config.load(config_file))
    trainer.train(training_data)
    model_directory = trainer.persist(model_dir, fixed_model_name ='aes_bot_v0')

train_bot('./data.json', 'config.yaml', './models/nlu')
