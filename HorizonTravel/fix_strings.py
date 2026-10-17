import os
import re

java_dir = r'app\src\main\java\com\example\horizontravel'
strings_xml = r'app\src\main\res\values\strings.xml'

with open(strings_xml, 'r', encoding='utf-8') as f:
    xml_content = f.read()

def add_string(name, value):
    global xml_content
    # check if exists
    if f'name="{name}"' in xml_content:
        return
    entry = f'    <string name="{name}">{value}</string>\n'
    xml_content = xml_content.replace('</resources>', entry + '</resources>')

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    orig = content
    
    # 1. setText("...") -> setText(R.string.xxx)
    # Note: setText in Android can take a string resource ID!
    def repl_settext(m):
        val = m.group(1)
        name = "text_" + re.sub(r'[^a-zA-Z0-9]', '_', val).strip('_').lower()
        if len(name) > 30: name = name[:30]
        add_string(name, val)
        return f'.setText(R.string.{name})'
    
    content = re.sub(r'\.setText\("([^"]+)"\)', repl_settext, content)
    
    # 2. Toast.makeText(ctx, "...", Toast.LENGTH_SHORT)
    # Toast.makeText can take a string resource ID!
    def repl_toast(m):
        prefix = m.group(1)
        val = m.group(2)
        suffix = m.group(3)
        name = "toast_" + re.sub(r'[^a-zA-Z0-9]', '_', val).strip('_').lower()
        if len(name) > 30: name = name[:30]
        add_string(name, val)
        return f'Toast.makeText({prefix}, R.string.{name}, {suffix})'
        
    content = re.sub(r'Toast\.makeText\(([^,]+),\s*"([^"]+)",\s*(Toast\.LENGTH_[A-Z]+)\)', repl_toast, content)
    
    # 3. setMessage("...") -> setMessage(R.string.xxx)
    def repl_msg(m):
        val = m.group(1)
        name = "msg_" + re.sub(r'[^a-zA-Z0-9]', '_', val).strip('_').lower()
        if len(name) > 30: name = name[:30]
        add_string(name, val)
        return f'.setMessage(R.string.{name})'
        
    content = re.sub(r'\.setMessage\("([^"]+)"\)', repl_msg, content)
    
    # 4. setTitle("...") -> setTitle(R.string.xxx)
    def repl_title(m):
        val = m.group(1)
        name = "title_" + re.sub(r'[^a-zA-Z0-9]', '_', val).strip('_').lower()
        if len(name) > 30: name = name[:30]
        add_string(name, val)
        return f'.setTitle(R.string.{name})'
        
    content = re.sub(r'\.setTitle\("([^"]+)"\)', repl_title, content)
    
    if orig != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated {filepath}")

for root, _, files in os.walk(java_dir):
    for f in files:
        if f.endswith('.java'):
            process_file(os.path.join(root, f))

with open(strings_xml, 'w', encoding='utf-8') as f:
    f.write(xml_content)
print("Updated strings.xml")
