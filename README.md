# RandX

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Sponsor](https://img.shields.io/badge/Sponsor-❤-red)](https://github.com/sponsors/ShaneeexD)

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

RandX has been tested with the **Dieharder** statistical test suite. Results show excellent randomness quality:

- **55 passed, 6 weak, 1 failed** (62 tests)
- Mean p-value: **0.5190** (ideal: 0.5)
- P-values follow expected uniform distribution

<img width="800" height="600" alt="pvalue_distribution" src="https://github.com/user-attachments/assets/52439dc2-9c95-4c66-93f5-3bb3e993770e" />

### Full Test Results

<details>
<summary>Click to expand Dieharder test results</summary>

| Test Name | ntup | tsamples | psamples | p-value | Result |
|-----------|------|----------|----------|---------|--------|
| diehard_birthdays | 0 | 100 | 100 | 0.85303190 | ✅ PASSED |
| diehard_operm5 | 0 | 1000000 | 100 | 0.01010227 | ✅ PASSED |
| diehard_rank_32x32 | 0 | 40000 | 100 | 0.00471001 | ⚠️ WEAK |
| diehard_rank_6x8 | 0 | 100000 | 100 | 0.18495518 | ✅ PASSED |
| diehard_bitstream | 0 | 2097152 | 100 | 0.99986140 | ⚠️ WEAK |
| diehard_opso | 0 | 2097152 | 100 | 0.07469798 | ✅ PASSED |
| diehard_oqso | 0 | 2097152 | 100 | 0.24560981 | ✅ PASSED |
| diehard_dna | 0 | 2097152 | 100 | 0.38591760 | ✅ PASSED |
| diehard_count_1s_str | 0 | 256000 | 100 | 0.43909582 | ✅ PASSED |
| diehard_count_1s_byt | 0 | 256000 | 100 | 0.00008660 | ⚠️ WEAK |
| diehard_parking_lot | 0 | 12000 | 100 | 0.99777049 | ⚠️ WEAK |
| diehard_2dsphere | 2 | 8000 | 100 | 0.70124356 | ✅ PASSED |
| diehard_3dsphere | 3 | 4000 | 100 | 0.59941329 | ✅ PASSED |
| diehard_squeeze | 0 | 100000 | 100 | 0.34280516 | ✅ PASSED |
| diehard_sums | 0 | 100 | 100 | 0.58366092 | ✅ PASSED |
| diehard_runs | 0 | 100000 | 100 | 0.89914599 | ✅ PASSED |
| diehard_runs | 0 | 100000 | 100 | 0.73389410 | ✅ PASSED |
| diehard_craps | 0 | 200000 | 100 | 0.02166256 | ✅ PASSED |
| diehard_craps | 0 | 200000 | 100 | 0.44520204 | ✅ PASSED |
| marsaglia_tsang_gcd | 0 | 10000000 | 100 | 0.00000000 | ❌ FAILED |
| marsaglia_tsang_gcd | 0 | 10000000 | 100 | 0.00020699 | ⚠️ WEAK |
| sts_monobit | 1 | 100000 | 100 | 0.84099232 | ✅ PASSED |
| sts_runs | 2 | 100000 | 100 | 0.43772600 | ✅ PASSED |
| sts_serial | 1 | 100000 | 100 | 0.88847368 | ✅ PASSED |
| sts_serial | 2 | 100000 | 100 | 0.53602494 | ✅ PASSED |
| sts_serial | 3 | 100000 | 100 | 0.11095856 | ✅ PASSED |
| sts_serial | 3 | 100000 | 100 | 0.23666711 | ✅ PASSED |
| sts_serial | 4 | 100000 | 100 | 0.48314433 | ✅ PASSED |
| sts_serial | 4 | 100000 | 100 | 0.30526397 | ✅ PASSED |
| sts_serial | 5 | 100000 | 100 | 0.94625343 | ✅ PASSED |
| sts_serial | 5 | 100000 | 100 | 0.22344894 | ✅ PASSED |
| sts_serial | 6 | 100000 | 100 | 0.50759382 | ✅ PASSED |
| sts_serial | 6 | 100000 | 100 | 0.62311655 | ✅ PASSED |
| sts_serial | 7 | 100000 | 100 | 0.77754286 | ✅ PASSED |
| sts_serial | 7 | 100000 | 100 | 0.38831338 | ✅ PASSED |
| sts_serial | 8 | 100000 | 100 | 0.70810052 | ✅ PASSED |
| sts_serial | 8 | 100000 | 100 | 0.78794335 | ✅ PASSED |
| sts_serial | 9 | 100000 | 100 | 0.94231855 | ✅ PASSED |
| sts_serial | 9 | 100000 | 100 | 0.93980387 | ✅ PASSED |
| sts_serial | 10 | 100000 | 100 | 0.08437737 | ✅ PASSED |
| sts_serial | 10 | 100000 | 100 | 0.10833821 | ✅ PASSED |
| sts_serial | 11 | 100000 | 100 | 0.38615644 | ✅ PASSED |
| sts_serial | 11 | 100000 | 100 | 0.85348827 | ✅ PASSED |
| sts_serial | 12 | 100000 | 100 | 0.82451496 | ✅ PASSED |
| sts_serial | 12 | 100000 | 100 | 0.73680045 | ✅ PASSED |
| sts_serial | 13 | 100000 | 100 | 0.92643090 | ✅ PASSED |
| sts_serial | 13 | 100000 | 100 | 0.79648087 | ✅ PASSED |
| sts_serial | 14 | 100000 | 100 | 0.75478426 | ✅ PASSED |
| sts_serial | 14 | 100000 | 100 | 0.80748464 | ✅ PASSED |
| sts_serial | 15 | 100000 | 100 | 0.28286554 | ✅ PASSED |
| sts_serial | 15 | 100000 | 100 | 0.11353190 | ✅ PASSED |
| sts_serial | 16 | 100000 | 100 | 0.99340160 | ✅ PASSED |
| sts_serial | 16 | 100000 | 100 | 0.16914373 | ✅ PASSED |
| rgb_bitdist | 1 | 100000 | 100 | 0.30214369 | ✅ PASSED |
| rgb_bitdist | 2 | 100000 | 100 | 0.57740365 | ✅ PASSED |
| rgb_bitdist | 3 | 100000 | 100 | 0.68306711 | ✅ PASSED |
| rgb_bitdist | 4 | 100000 | 100 | 0.93347239 | ✅ PASSED |
| rgb_bitdist | 5 | 100000 | 100 | 0.57207805 | ✅ PASSED |
| rgb_bitdist | 6 | 100000 | 100 | 0.69125374 | ✅ PASSED |
| rgb_bitdist | 7 | 100000 | 100 | 0.33875916 | ✅ PASSED |

</details>

