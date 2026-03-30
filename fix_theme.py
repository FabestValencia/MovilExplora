import os
import glob

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # We want to replace standard background color
    content = content.replace("Modifier.background(Color.White)", "Modifier.background(MaterialTheme.colorScheme.background)")
    content = content.replace("containerColor = Color.White", "containerColor = MaterialTheme.colorScheme.surface")

    # Text colors
    content = content.replace("color = DarkBlue", "color = MaterialTheme.colorScheme.onBackground")
    content = content.replace("tint = DarkBlue", "tint = MaterialTheme.colorScheme.onBackground")
    # specific cases of Border
    content = content.replace("border(2.dp, DarkBlue,", "border(2.dp, MaterialTheme.colorScheme.onBackground,")

    # Add MaterialTheme import if not there
    if "MaterialTheme." in content and "import androidx.compose.material3.MaterialTheme" not in content:
        if "import androidx.compose.material3.*" in content:
            content = content.replace("import androidx.compose.material3.*", "import androidx.compose.material3.*\nimport androidx.compose.material3.MaterialTheme")
        else:
            lines = content.split('\n')
            for i, line in enumerate(lines):
                if line.startswith("import androidx.compose."):
                    lines.insert(i, "import androidx.compose.material3.MaterialTheme")
                    break
            content = '\n'.join(lines)

    # remove unused import
    content = content.replace("import com.example.movilexplora.ui.theme.DarkBlue\n", "")

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

for root, _, files in os.walk("app/src/main/java/com/example/movilexplora/features"):
    for file in files:
        if file.endswith(".kt"):
            process_file(os.path.join(root, file))

print("Done")
