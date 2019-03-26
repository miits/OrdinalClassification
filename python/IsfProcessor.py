import re

from python.IsfMetadata import IsfMetadata


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
