# google-cloud-OCR-wordMappingToLines
Map the OCR extracted text to lines ( using Google Cloud OCR API results)

Google Cloud Optical Recognition API detects and extracts text from any image 
and returns a string (json-like) random result of the words detected and their coordinations in x-y axis.

The function provided here maps the words in lines and according to the original order so that the
resulting html-string of the words will look alike the original image.
