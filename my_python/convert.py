import os
import json
import ast

INPUT_BASE = "dataset-nft-FULL"
OUTPUT_BASE = "jsonCollections"
SUBDIRS = ["pfp", "gaming"]

def convert_file(input_path, output_path):
    with open(input_path, "r") as fin, open(output_path, "w") as fout:
        for line_number, line in enumerate(fin, start=1):
            line = line.strip()

            if not line:
                continue

            try:
                python_obj = ast.literal_eval(line)

                # opzionale: assicura che sia un dizionario
                if not isinstance(python_obj, dict):
                    continue

                json_line = json.dumps(python_obj, separators=(',', ':'))
                fout.write(json_line + "\n")

            except (SyntaxError, ValueError):
                print(f"⚠️  {input_path} | riga {line_number} ignorata")

def main():
    for subdir in SUBDIRS:
        input_dir = os.path.join(INPUT_BASE, subdir)
        output_dir = os.path.join(OUTPUT_BASE, subdir)

        if not os.path.isdir(input_dir):
            print(f"❌ Directory input non trovata: {input_dir}")
            continue

        # Crea la directory di output se non esiste
        os.makedirs(output_dir, exist_ok=True)

        for filename in os.listdir(input_dir):
            input_path = os.path.join(input_dir, filename)

            if not os.path.isfile(input_path):
                continue

            output_path = os.path.join(output_dir, filename + ".json")

            print(f"➡️  Converto {input_path}")
            convert_file(input_path, output_path)

    print("\n✅ Conversione COMPLETATA")

if __name__ == "__main__":
    main()