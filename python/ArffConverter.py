import os
import re
import json
import csv
from scipy.io import arff

ARFF_PATH = os.path.join('..', 'data', 'nonOrdinal', 'gen')
CSV_PATH = os.path.join('..', 'data', 'nonOrdinal', 'gen', 'csv')
JSON_PATH = os.path.join('..', 'data', 'nonOrdinal', 'gen', 'json')


def arff_to_csv_and_json(filename):
    path_to_file = os.path.join(ARFF_PATH, filename)
    data, metadata = arff.loadarff(path_to_file)
    arff_attributes = metadata._attributes
    attributes = transform_arff_attributes(arff_attributes)
    data = decode_bytes_to_strings(data)
    json_file_path = os.path.join(JSON_PATH, re.sub('\.arff', '.json', filename))
    csv_file_path = os.path.join(CSV_PATH, re.sub('\.arff', '.csv', filename))
    save_as_json(attributes, json_file_path)
    save_as_csv(data, csv_file_path)


def transform_arff_attributes(arff_attributes):
    attributes = []
    for attr_name, attr_type in arff_attributes.items():
        attribute = new_attribute(attr_name)
        if attr_type[0] == 'numeric':
            attribute['valueType'] = 'real'
        else:
            attribute['valueType'] = 'enumeration'
            attribute['domain'] = list(attr_type[1])
        attributes.append(attribute)
    attributes[-1]['type'] = 'decision'
    return attributes


def decode_bytes_to_strings(data):
    out = []
    types = data.dtype.descr
    for object_idx, row in enumerate(data):
        new_row = list(row)
        for field_idx, type in enumerate(types):
            if type[1].startswith('|S'):
                decoded_value = row[field_idx].decode('utf-8')
                new_row[field_idx] = decoded_value
        new_row = tuple(new_row)
        out.append(new_row)
    return out


def new_attribute(name):
    return dict({'name': name, 'active': True, 'valueType': None, 'preferenceType': 'gain', 'type': 'condition'})


def save_as_json(attributes, json_file_path):
    with open(json_file_path, 'w') as file:
        json.dump(attributes, file, indent=4)


def save_as_csv(data, csv_file_path):
    with open(csv_file_path, 'w', newline='') as file:
        writer = csv.writer(file, delimiter=',')
        writer.writerows(data)


for filename in os.listdir(ARFF_PATH):
    if filename.endswith('.arff'):
        arff_to_csv_and_json(filename)
    else:
        continue
