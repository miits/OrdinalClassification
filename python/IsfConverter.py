import os
import re
import json
import csv

from python.IsfProcessor import IsfProcessor

ISF_PATH = os.path.join('data', 'isf')
CSV_PATH = os.path.join('data', 'csv')
JSON_PATH = os.path.join('data', 'json')


def isf_to_csv_and_json(filename, isf_delimiter):
    path_to_file = os.path.join(ISF_PATH, filename)
    file = open(path_to_file, 'r')
    processor = IsfProcessor()
    for line in file.readlines():
        processor.process_line(line)
    metadata = processor.get_metadata()
    data = processor.get_data()
    json_file_path = os.path.join(JSON_PATH, re.sub('\.isf', '.json', filename))
    csv_file_path = os.path.join(CSV_PATH, re.sub('\.isf', '.csv', filename))
    save_as_json(metadata, json_file_path)
    save_as_csv(data, csv_file_path, isf_delimiter)


def save_as_json(isf_metadata, json_file_path):
    with open(json_file_path, 'w') as file:
        json.dump(isf_metadata.attributes, file, indent=4)


def save_as_csv(data, csv_file_path, isf_delimiter):
    with open(csv_file_path, 'w', newline='') as file:
        writer = csv.writer(file, delimiter=',')
        rows = [line.split(isf_delimiter) for line in data]
        writer.writerows(rows)


delimiters = dict({
    'balance_scale.isf': ',',
    'breast-w.isf': '\t',
    'car.isf': '\t',
    'cpu.isf': '\t',
    'dataset1_noid.isf': '\t',
    'dataset3.isf': '\t',
    'denbosch.isf': '\t',
    'ERA_n.isf': ',',
    'ESL_n.isf': ',',
    'housing.isf': ',',
    'LEV_n.isf': ',',
    'SWD_n.isf': ',',
    'windsor.isf': '\t'
})
for filename in os.listdir(ISF_PATH):
    if filename.endswith('.isf'):
        isf_to_csv_and_json(filename, delimiters[filename])
    else:
        continue
