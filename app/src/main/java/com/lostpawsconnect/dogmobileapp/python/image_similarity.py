import requests
from PIL import Image
import io
import tensorflow as tf
from tensorflow.keras.applications import VGG16
from tensorflow.keras.applications.vgg16 import preprocess_input
from tensorflow.keras.preprocessing import image
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity

def image_to_vector(img, model=None):
    if model is None:
        model = VGG16(weights='imagenet', include_top=False)

    img = img.resize((224,224))
    img_array = image.img_to_array(img)
    img_array = np.expand_dims(img_array, axis=0)
    img_array = preprocess_input(img_array)

    features = model.predict(img_array)
    vector_representation = features.flatten()

    return vector_representation

def compare_similarity(url1, url2):
    image1 = load_image_from_url(url1)
    image2 = load_image_from_url(url2)

    # Convert web images to vectors
    vector1 = image_to_vector(image1)
    vector2 = image_to_vector(image2)
    # Calculate the Structural Similarity Index (SSIM)
    similarity_score = cosine_similarity([vector1], [vector2])[0][0]

    return similarity_score

def load_image_from_url(image_url):
    response = requests.get(image_url)
    image = Image.open(io.BytesIO(response.content))
    return image
