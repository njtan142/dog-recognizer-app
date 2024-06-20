
import requests
import cv2
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity


def image_to_vector(img_path):
    # Load the image using OpenCV
    nparr = np.frombuffer(img_path.content, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    img = cv2.resize(img, (224,224))
    gray_img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    # Extract HOG features
    hog = cv2.HOGDescriptor()
    vector_representation = hog.compute(gray_img).flatten()

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
    return response