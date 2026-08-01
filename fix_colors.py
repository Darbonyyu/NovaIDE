with open('./app/src/main/res/values/colors.xml', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if "purple_200" in line or "purple_500" in line or "purple_700" in line or "teal_200" in line or "teal_700" in line or "black" in line or "white" in line:
        continue
    new_lines.append(line)

with open('./app/src/main/res/values/colors.xml', 'w') as f:
    f.writelines(new_lines)
