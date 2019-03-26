import re


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
