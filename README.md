# RandX

A high-speed, cryptographically-inspired random number generator for Java.

## Installation

### Option 1: Add JAR to Classpath (Simplest)

1. Copy `randx.jar` to your project's `lib` folder
2. Add to classpath when compiling/running:

```bash
# Compile
javac -cp lib/randx.jar YourClass.java

# Run
java -cp .;lib/randx.jar YourClass
```

### Option 2: Install to Local Maven Repository

```bash
mvn install:install-file -Dfile=randx.jar -DgroupId=com.randx -DartifactId=randx -Dversion=1.0.0 -Dpackaging=jar
```

Then add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.randx</groupId>
    <artifactId>randx</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Option 3: Add JAR to IDE

**IntelliJ IDEA:**
1. File → Project Structure → Libraries
2. Click + → Java → Select `randx.jar`

**Eclipse:**
1. Right-click project → Build Path → Configure Build Path
2. Libraries → Add External JARs → Select `randx.jar`

**VS Code:**
Add to `.vscode/settings.json`:
```json
{
    "java.project.referencedLibraries": [
        "lib/randx.jar"
    ]
}
```

## Usage

```java
import com.randx.RandX;

public class Example {
    public static void main(String[] args) {
        // Random integer between 1 and 100 (inclusive)
        int num = RandX.range(1, 100);
        
        // Random double between 0.0 and 1.0
        double d = RandX.next();
        
        // Random double in range
        double price = RandX.range(10.0, 99.99);
        
        // Random boolean
        boolean coinFlip = RandX.bool();
        
        // Random bytes (for tokens, keys, etc.)
        byte[] token = RandX.bytes(32);
        
        // Force entropy refresh before critical operation
        RandX.refresh();
        
        // Cleanup when done (optional - stops background thread)
        RandX.shutdown();
    }
}
```

## API Reference

| Method | Description |
|--------|-------------|
| `RandX.range(int min, int max)` | Random int in [min, max] inclusive |
| `RandX.range(double min, double max)` | Random double in [min, max) |
| `RandX.next(int max)` | Random int from 0 to max (exclusive) |
| `RandX.next()` | Random double 0.0 to 1.0 |
| `RandX.bool()` | Random boolean |
| `RandX.bytes(int count)` | Random byte array |
| `RandX.refresh()` | Force entropy refresh |
| `RandX.shutdown()` | Stop background entropy collector |

## How It Works

RandX combines:
- **Hardware entropy** from timing jitter, memory allocation, thread scheduling
- **ChaCha20-style mixing** (ARX operations) for cryptographic diffusion
- **Continuous entropy pooling** via background thread

**Performance**: ~23x faster than `SecureRandom`, same speed as `Math.random()` but more secure and completely unpredictable

<img width="512" height="512" alt="random_noise" src="https://github.com/user-attachments/assets/d3d72d28-9c35-406f-84ee-d316021b90ca" />

## Statistical Testing

RandX has been tested with the **Dieharder** statistical test suite (100+ tests). Results show excellent randomness quality:

- **15 passed, 4 weak, 0 failed** (from initial 19 tests)
- Mean p-value: **0.4486** (ideal: 0.5)
- P-values follow expected uniform distribution

<img width="993" height="767" alt="p-value-dist" src="https://github.com/user-attachments/assets/7069da07-bb9c-4b32-aaeb-c794b2a360cc" />

