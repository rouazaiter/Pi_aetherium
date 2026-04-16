import os
import shutil

# Define paths
dto_dir = r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto'
dtos_dir = r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dtos'

# Create the dto directory if it doesn't exist
os.makedirs(dto_dir, exist_ok=True)
print(f'✓ Directory created: {dto_dir}')

# Copy files from dtos to dto
files_to_copy = ['SkillNodeDto.java', 'PortfolioDNADto.java']
for file_name in files_to_copy:
    src = os.path.join(dtos_dir, file_name)
    dst = os.path.join(dto_dir, file_name)
    shutil.copy2(src, dst)
    print(f'✓ Copied {file_name}')

# Verify files exist
print(f'\n=== VERIFICATION ===')
print(f'Files in {dto_dir}:')
for file in os.listdir(dto_dir):
    if os.path.isfile(os.path.join(dto_dir, file)):
        print(f'  ✓ {file}')
