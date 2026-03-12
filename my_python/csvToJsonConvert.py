import csv
import json
import os
from datetime import datetime

INPUT_DIR = "Dataset"
OUTPUT_DIR = "DatasetJson_filtered"

NULL_ADDRESS = "0x0000000000000000000000000000000000000000"


def iso_to_epoch(ts):
    try:
        dt = datetime.fromisoformat(ts.replace("Z", "+00:00"))
        return int(dt.timestamp())
    except:
        return 0


def convert_csv_to_json(input_path, output_path):

    with open(input_path, newline='', encoding="utf-8") as fin, \
         open(output_path, "w", encoding="utf-8") as fout:

        reader = csv.DictReader(fin)

        for line_number, row in enumerate(reader, start=2):

            seller = row.get("from_address")
            buyer = row.get("to_address")

            seller_entity = row.get("from_address_entity")
            buyer_entity = row.get("to_address_entity")

            timestamp = iso_to_epoch(row.get("block_timestamp", ""))

            token_address = row.get("token_address")
            token_id = row.get("token_id")
            #token_hash = row.get("token_address")

            possible_spam = row.get("possible_spam")

            # ------------------------
            # FILTRI
            # ------------------------

            # dati mancanti
            if not seller or not buyer or not token_address or not token_id: #token_hash:
                print(f"{input_path} - skipped line {line_number} (missing fields)")
                continue

            # mint
            if seller.lower() == NULL_ADDRESS:
                #print(f"{input_path} - skipped line {line_number} (mint)")
                continue

            # burn
            if buyer.lower() == NULL_ADDRESS:
                #print(f"{input_path} - skipped line {line_number} (burn)")
                continue

            """
            # token_id corrotti (float scientifici)
            if "e+" in token_id.lower():
                continue"""

            # spam nft
            if possible_spam and possible_spam.lower() == "true":
                continue

            # interazioni con smart contract
            if seller_entity:
                print(f"{input_path} - skipped line {line_number} ({seller_entity})")
                continue

            if buyer_entity:
                print(f"{input_path} - skipped line {line_number} ({buyer_entity})")
                continue

            # ------------------------

            identifier = f"{token_id}" #f"{token_hash}" 

            event = {
                "asset_events": [
                    {
                        "seller": seller,
                        "buyer": buyer,
                        "event_timestamp": timestamp,
                        "nft": {
                            "identifier": identifier
                        }
                    }
                ]
            }

            fout.write(json.dumps(event) + "\n")


def main():

    os.makedirs(OUTPUT_DIR, exist_ok=True)

    for filename in os.listdir(INPUT_DIR):

        if not filename.endswith(".csv"):
            continue

        input_path = os.path.join(INPUT_DIR, filename)

        json_name = filename.replace(".csv", ".json")
        output_path = os.path.join(OUTPUT_DIR, json_name)

        print(f"Converting {filename} → {json_name}")

        convert_csv_to_json(input_path, output_path)

    print("\nConversion completed!")


if __name__ == "__main__":
    main()
