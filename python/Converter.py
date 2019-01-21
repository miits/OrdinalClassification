import os
import re
import json
import csv


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


class IsfMetadata:
    def __init__(self):
        self.attributes = []
        self.attribute_list_indices = dict()

    def add_attr_value_type(self, name, value_type):
        if name not in self.attribute_list_indices:
            new_attr_idx = len(self.attributes)
            self.attribute_list_indices[name] = new_attr_idx
            self.attributes.append(self._new_attribute(name))
        value_type = self._get_value_type(value_type)
        if isinstance(value_type, list):
            self.attributes[self.attribute_list_indices[name]]['valueType'] = value_type[0]
            self.attributes[self.attribute_list_indices[name]]['domain'] = value_type[1]
        else:
            self.attributes[self.attribute_list_indices[name]]['valueType'] = value_type

    def add_attr_preference(self, name, pref_type):
        if name not in self.attribute_list_indices:
            new_attr_idx = len(self.attributes)
            self.attribute_list_indices[name] = new_attr_idx
            self.attributes.append(self._new_attribute(name))
        self.attributes[self.attribute_list_indices[name]]['preferenceType'] = pref_type

    def set_decision_attr(self, name):
        if name not in self.attribute_list_indices:
            new_attr_idx = len(self.attributes)
            self.attribute_list_indices[name] = new_attr_idx
            new_attr = self._new_attribute(name)
            new_attr['type'] = 'decision'
            self.attributes.append(new_attr)
        else:
            self.attributes[self.attribute_list_indices[name]]['type'] = 'decision'

    def _get_value_type(self, value_type):
        value_type = re.sub('[()]', '', value_type)
        if value_type == 'integer':
            return value_type
        elif value_type == 'continuous':
            return 'real'
        else:
            return ['enumeration', self._list_string_to_list(value_type)]

    @staticmethod
    def _list_string_to_list(list_string):
        new_list = []
        list_string = re.sub('[\[\]]', '', list_string)
        [new_list.append(value) for value in list_string.split(sep=',')]
        return new_list

    @staticmethod
    def _new_attribute(name):
        return dict({'name': name, 'active': True, 'valueType': None, 'preferenceType': None, 'type': 'condition'})


class IsfProcessor:
    def __init__(self):
        self._metadata = IsfMetadata()
        self._data = []
        self._current_section = None
        self._section_data_adders_by_name = dict({
            '**ATTRIBUTES': self._add_attribute,
            '**PREFERENCES': self._add_preference,
            '**EXAMPLES': self._add_example
        })

    def process_line(self, line):
        line = self._cut_eol(line)
        line = line.strip()
        if line.startswith('**END'):
            return
        elif line.startswith('**'):
            self.process_section_header(line)
        elif line != '':
            self.process_section_data(line)

    def process_section_header(self, line):
        self._current_section = line

    def process_section_data(self, line):
        self._section_data_adders_by_name[self._current_section](line)

    def get_metadata(self):
        return self._metadata

    def get_data(self):
        return self._data

    @staticmethod
    def _cut_eol(line):
        return re.sub('\n', '', line)

    def _add_attribute(self, line):
        attr_name = self._get_attr_name(line)
        value = self._get_value(line)
        if attr_name == 'decision':
            self._metadata.set_decision_attr(value)
        else:
            self._metadata.add_attr_value_type(attr_name, value)

    @staticmethod
    def _get_attr_name(line):
        separator_pos = line.find(':')
        attr_name = line[:separator_pos]
        attr_name = re.sub('\+', '', attr_name)
        attr_name = re.sub(' ', '', attr_name)
        return attr_name

    @staticmethod
    def _get_value(line):
        separator_pos = line.find(':')
        attr_type = line[separator_pos + 1:]
        attr_type = re.sub(' ', '', attr_type)
        return attr_type

    def _add_preference(self, line):
        attr_name = self._get_attr_name(line)
        value = self._get_value(line)
        self._metadata.add_attr_preference(attr_name, value)

    def _add_example(self, line):
        self._data.append(line)


delimiters = dict({
    'balance_scale.isf': ',',
    'breast-w.isf': ' ',
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
