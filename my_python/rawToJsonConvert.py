import json
import os
from datetime import datetime

INPUT_ROOT = "my_python/DatasetRaw"
OUTPUT_ROOT = "my_python/DatasetJson_raw_unique"
#INPUT_ROOT = "my_python/DatasetExtra"
#OUTPUT_ROOT = "my_python/DatasetJson_extra"


def iso_to_epoch(ts):
    try:
        dt = datetime.fromisoformat(ts.replace("Z", "+00:00"))
        return int(dt.timestamp())
    except:
        return 0


def extract_index(filename):
    """
    Estrae il numero finale dal filename per ordinamento corretto
    es: axie_infinity_transactions_collection_12.json -> 12
    """
    try:
        return int(filename.split("_")[-1].replace(".json", ""))
    except:
        return -1


def convert_collection(collection_path, output_file):

    files = [f for f in os.listdir(collection_path) if f.endswith(".json")]

    # Ordinamento corretto: 0,1,2,3,...
    files.sort(key=extract_index)

    # 🔥 struttura per deduplicazione globale della collezione
    seen_transactions = {}
    duplicate_count = 0

    with open(output_file, "w", encoding="utf-8") as fout:

        for filename in files:

            file_path = os.path.join(collection_path, filename)

            with open(file_path, "r", encoding="utf-8") as fin:
                try:
                    data = json.load(fin)
                except:
                    print(f"Errore lettura {filename}")
                    continue

            events = []

            for tx in data:

                seller = tx.get("from_address")
                buyer = tx.get("to_address")

                seller_entity = tx.get("from_address_entity")
                buyer_entity = tx.get("to_address_entity")

                token_id = tx.get("token_id")
                timestamp = iso_to_epoch(tx.get("block_timestamp", ""))

                possible_spam = tx.get("possible_spam")

                block_hash = tx.get("block_hash")
                tx_hash = tx.get("transaction_hash")

                # ------------------------
                # FILTRI
                # ------------------------

                # dati mancanti
                if not seller or not buyer or not token_id or not block_hash or not tx_hash:
                    continue

                # skip se ci sono entity
                if seller_entity is not None or buyer_entity is not None:
                    continue

                # spam
                if possible_spam is True:
                    continue

                # ------------------------
                # FILTRO DUPLICATI
                # ------------------------

                if block_hash not in seen_transactions:
                    seen_transactions[block_hash] = set()

                if tx_hash in seen_transactions[block_hash]:
                    #print(f"skip {tx_hash} bc duplicated")
                    duplicate_count += 1
                    continue  # duplicato

                seen_transactions[block_hash].add(tx_hash)

                # ------------------------

                event = {
                    "seller": seller,
                    "buyer": buyer,
                    "event_timestamp": timestamp,
                    "nft": {
                        "identifier": str(token_id)
                    }
                }

                events.append(event)

            # scrive UNA riga = un wrapper per file
            wrapper = {
                "asset_events": events
            }

            fout.write(json.dumps(wrapper) + "\n")

    print(f"Duplicati rimossi: {duplicate_count}")
    print(f"Creato: {output_file}")


def main():

    os.makedirs(OUTPUT_ROOT, exist_ok=True)

    for collection in os.listdir(INPUT_ROOT):

        collection_path = os.path.join(INPUT_ROOT, collection)

        if not os.path.isdir(collection_path):
            continue

        output_file = os.path.join(OUTPUT_ROOT, f"{collection}.json")

        print(f"Processing collection: {collection}")

        convert_collection(collection_path, output_file)

    print("\nConversion completata!")


if __name__ == "__main__":
    main()