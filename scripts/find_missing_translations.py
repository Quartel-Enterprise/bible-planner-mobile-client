import os
import xml.etree.ElementTree as ET
import glob

def get_string_keys(file_path):
    keys = set()
    if not os.path.exists(file_path):
        return keys
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        for string in root.findall('string'):
            if string.get('translatable') == 'false':
                continue
            name = string.get('name')
            if name:
                keys.add(name)
        # Also handle plurals if they exist, though prompt asked for 'string resources' generally
        for plural in root.findall('plurals'):
            name = plural.get('name')
            if name:
                keys.add(name)
    except Exception as e:
        print(f"Error parsing {file_path}: {e}")
    return keys

def find_missing_translations(root_dir):
    # Find all default strings.xml files
    # Pattern seems to be .../values/strings.xml
    # We will search recursively
    default_files = glob.glob(os.path.join(root_dir, '**', 'values', 'strings.xml'), recursive=True)
    
    missing_report = {}

    for default_file in default_files:
        values_dir = os.path.dirname(default_file)
        # values_dir is like .../composeResources/values
        base_resources_dir = os.path.dirname(values_dir)
        # base_resources_dir is like .../composeResources

        # Define expected locales
        locales = {
            'es': 'values-es',
            'pt-rBR': 'values-pt-rBR'
        }

        default_keys = get_string_keys(default_file)
        if not default_keys:
            continue

        module_name = default_file.replace(root_dir, '')
        
        for lang, folder_name in locales.items():
            localized_file = os.path.join(base_resources_dir, folder_name, 'strings.xml')
            localized_keys = get_string_keys(localized_file)
            
            missing = default_keys - localized_keys
            if missing:
                if module_name not in missing_report:
                    missing_report[module_name] = {}
                missing_report[module_name][lang] = list(missing)

    return missing_report

if __name__ == "__main__":
    root_directory = os.getcwd() # Run from current directory
    report = find_missing_translations(root_directory)
    
    if not report:
        print("No missing translations found.")
    else:
        for module, diffs in report.items():
            print(f"\nModule: {module}")
            for lang, keys in diffs.items():
                print(f"  Missing in {lang}:")
                for key in sorted(keys):
                    print(f"    - {key}")
