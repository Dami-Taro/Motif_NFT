import json
import ast

input_file = "boredapeyachtclub.txt"
output_file = "output_file.json"

with open(input_file, "r") as fin, open(output_file, "w") as fout:
    for line_number, line in enumerate(fin, start=1):
        line = line.strip()

        # Salta righe vuote
        if not line:
            continue

        try:
            # Prova a interpretare la riga come dizionario Python
            python_obj = ast.literal_eval(line)

            # Converti in JSON compatto (una sola linea)
            json_line = json.dumps(python_obj, separators=(',', ':'))

            # Scrive una riga JSON
            fout.write(json_line + "\n")

        except (SyntaxError, ValueError):
            # Riga non convertibile → ignorata
            print(f"⚠️ Riga {line_number} ignorata (non è un dict valido)")

print("✅ Conversione riga per riga completata!")
