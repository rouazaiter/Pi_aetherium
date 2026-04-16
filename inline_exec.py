import os
import subprocess

os.chdir(r'C:\Users\jihen\Downloads\portfolio')

# Compile
r1 = subprocess.run(['javac', 'SetupDTOFiles.java'], capture_output=True, text=True)
print("COMPILE OUTPUT:")
print(r1.stdout if r1.stdout else "(no output)")
if r1.stderr:
    print("STDERR:", r1.stderr)
print("Return code:", r1.returncode)
print()

# Run
r2 = subprocess.run(['java', 'SetupDTOFiles'], capture_output=True, text=True)
print("RUN OUTPUT:")
print(r2.stdout)
if r2.stderr:
    print("STDERR:", r2.stderr)
print()

# Verify
dto_dir = r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto'
print("VERIFICATION:")
print(f"Directory exists: {os.path.exists(dto_dir)}")
if os.path.exists(dto_dir):
    files = os.listdir(dto_dir)
    print(f"Files: {files}")
    
    for fn in files:
        fp = os.path.join(dto_dir, fn)
        if os.path.isfile(fp):
            print(f"\n--- {fn} ---")
            with open(fp, 'r') as f:
                print(f.read())
