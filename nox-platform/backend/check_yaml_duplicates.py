import yaml
import sys

class UniqueKeyLoader(yaml.SafeLoader):
    def construct_mapping(self, node, deep=False):
        mapping = []
        for key_node, value_node in node.value:
            key = self.construct_object(key_node, deep=deep)
            if key in [m[0] for m in mapping]:
                print(f"Duplicate key found: {key}")
            mapping.append((key, self.construct_object(value_node, deep=deep)))
        return super().construct_mapping(node, deep)

def check_yaml(filepath):
    print(f"Checking {filepath}...")
    try:
        with open(filepath, 'r') as f:
            yaml.load(f, Loader=UniqueKeyLoader)
    except Exception as e:
        print(f"Error parsing {filepath}: {e}")

if __name__ == "__main__":
    for arg in sys.argv[1:]:
        check_yaml(arg)
