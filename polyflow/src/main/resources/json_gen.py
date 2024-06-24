import json
from urllib.request import Request, urlopen
import os
def main():
    pokedex = []
    fields = ['id', 'name', 'game_indices']
    f = open("test.json", "w")
    url = "https://pokeapi.co/api/v2/pokemon/"
    for i in range(1, 152):
        req = Request(url+str(i), headers={'User-Agent': 'Mozilla/5.0'})
        response = urlopen(req)
        data = json.loads(response.read())
        pokedex.append({field:data[field] for field in fields})
    json.dump(pokedex, f, indent=4)
if __name__ == "__main__":
    main()