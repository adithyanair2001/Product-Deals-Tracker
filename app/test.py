#!/usr/bin/python3

from bs4 import BeautifulSoup as bs
import requests

HEADERS = ({'User-Agent': 'Mozilla/5.0 (X11; Linux x86_64)', 'Accept-Language': 'en-US, en;q=0.5'})

class OBJ:
	def __init__(self, URL):
		self.URL = URL
		self.ofr = []
		self.update()

	def update(self):
		self.pge = bs(requests.get(self.URL, headers=HEADERS).text, 'html.parser')
		self.ttl = self.pge.find("title").text
		return self.check(self.pge.find_all("span", class_="description"))

	def check(self, ofr):
		ot = len(self.ofr)!=len(ofr) or any([i.text != j.text for i, j in zip(self.ofr, ofr)])
		self.ofr = ofr
		return ot

	def show(self):
		d = "-"
		print(f"{d*50}\n{self.ttl}\n{d*50}\n{len(self.ofr)} offers found!\n")
		for i in self.ofr:
			print(f"{d} {i.text}", "\n")

def main(URL):
	lst = [OBJ(i) for i in URL]
	for i in lst:
		i.show()

if __name__ == '__main__':
	with open("URLs.txt", "r") as f:
		URL = f.read().strip().split('\n')
	main(URL)