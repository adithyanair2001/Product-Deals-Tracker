#!/usr/bin/python3

from flask import Flask, render_template, request
app = Flask(__name__, template_folder = 'template')

def add(url):
	with open('URLs.txt', 'a') as f:
		f.write(url+'\n')

@app.route('/', methods = ["GET", "POST"])
def home():
	if request.method == "POST":
		url = request.form.get('url')
		add(url)
	return render_template('index.html')

if __name__ == '__main__':
	app.run(host="localhost", port=6080)
