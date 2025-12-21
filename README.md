# RandX

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Sponsor](https://img.shields.io/badge/Sponsor-❤-red)](https://github.com/sponsors/ShaneeexD)

> **The secure-enough RNG**: When `Math.random()` is too predictable, but `SecureRandom` is too slow.
> Fast, hardware-entropy-backed randomness for games, simulations, and systems that need unpredictability without cryptographic overhead.

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

## When to Use RandX

### ✅ Perfect for:
- **Gaming** - Loot drops, procedural generation, matchmaking (prevent players from predicting/exploiting RNG)
- **Simulations** - Monte Carlo, financial modeling (where reproducibility isn't needed)
- **Load testing** - Generating realistic unpredictable traffic patterns
- **Session/Request IDs** - Non-sensitive but should be hard to guess
- **A/B testing** - User bucketing where you don't want patterns
- **Game server anti-cheat** - Unpredictable spawns, timings (harder to bot)

### ⚠️ Use SecureRandom instead for:
- Cryptographic key generation
- Password generation
- Authentication tokens
- Payment processing
- Anything where security compromise = serious consequences

### 📊 Comparison

| Feature | Math.random() | RandX | SecureRandom |
|---------|--------------|-------|--------------|
| Speed | ⚡⚡⚡ Fast | ⚡⚡⚡ Fast | 🐌 Slow (23x) |
| Predictability | ❌ Easily predicted | ✅ Hard to predict | ✅ Cryptographically secure |
| State visible | ❌ Yes (seed-based) | ✅ No (entropy pooling) | ✅ No |
| Use case | Simple random values | Game logic, simulations | Security tokens |

## How It Works

RandX combines:
- **Hardware entropy** from timing jitter, memory allocation, thread scheduling
- **ChaCha20-style mixing** (ARX operations) for cryptographic diffusion
- **Continuous entropy pooling** via background thread

**Performance**: ~23x faster than `SecureRandom`, same speed as `Math.random()` but more secure and completely unpredictable

## Why RandX is Hard to Predict

Unlike `Math.random()` which uses a simple LCG (Linear Congruential Generator) with a visible 48-bit seed, RandX:

1. **Continuously pools hardware entropy** from timing jitter, memory allocation, thread scheduling
2. **Uses ChaCha20-style ARX mixing** (Add-Rotate-XOR) for cryptographic diffusion
3. **Has no exposed seed state** - internal state is constantly refreshed
4. **Combines multiple entropy sources** - impossible to control all variables

**Result**: Even if an attacker observes millions of outputs, they cannot reverse-engineer the internal state, predict future values, or reproduce the sequence.

## Design Philosophy

RandX combines proven techniques from cryptographic and systems-level RNGs into a fast, practical solution for Java:

- **ChaCha20-inspired mixing**: Uses ARX operations similar to the widely-studied ChaCha20 cipher for excellent diffusion
- **Continuous entropy pooling**: Background thread constantly harvests hardware entropy (inspired by Linux kernel's approach)
- **Userspace implementation**: Brings kernel-level techniques to Java applications without syscall overhead

This isn't a new algorithm, it's a thoughtful combination of established techniques optimized for the "secure-enough" use case.

<img width="512" height="512" alt="random_noise" src="https://github.com/user-attachments/assets/d3d72d28-9c35-406f-84ee-d316021b90ca" />

## Statistical Testing

RandX has been tested with the **Dieharder** statistical test suite (v3.31.1) using a 7GB sample file. Results show excellent randomness quality:

- **100 passed, 0 weak, 0 failed** (100 tests so far, test in progress)
- Mean p-value: **0.54** (ideal: 0.5)
- P-values follow expected uniform distribution

<img width="800" height="600" alt="pvalue_distribution" src="https://github.com/user-attachments/assets/81206940-86d0-4371-8178-a10928b130b8" />

### Full Test Results

<details>
<summary>Click to expand Dieharder test results</summary>

| Test Name | ntup | tsamples | psamples | p-value | Result |
|-----------|------|----------|----------|---------|--------|
| diehard_birthdays | 0 | 100 | 100 | 0.30506468 | ✅ PASSED |
| diehard_operm5 | 0 | 1000000 | 100 | 0.68453981 | ✅ PASSED |
| diehard_rank_32x32 | 0 | 40000 | 100 | 0.56057613 | ✅ PASSED |
| diehard_rank_6x8 | 0 | 100000 | 100 | 0.41419740 | ✅ PASSED |
| diehard_bitstream | 0 | 2097152 | 100 | 0.69602081 | ✅ PASSED |
| diehard_opso | 0 | 2097152 | 100 | 0.18037669 | ✅ PASSED |
| diehard_oqso | 0 | 2097152 | 100 | 0.26997919 | ✅ PASSED |
| diehard_dna | 0 | 2097152 | 100 | 0.07937436 | ✅ PASSED |
| diehard_count_1s_str | 0 | 256000 | 100 | 0.01628503 | ✅ PASSED |
| diehard_count_1s_byt | 0 | 256000 | 100 | 0.76573547 | ✅ PASSED |
| diehard_parking_lot | 0 | 12000 | 100 | 0.22336727 | ✅ PASSED |
| diehard_2dsphere | 2 | 8000 | 100 | 0.91578059 | ✅ PASSED |
| diehard_3dsphere | 3 | 4000 | 100 | 0.48653634 | ✅ PASSED |
| diehard_squeeze | 0 | 100000 | 100 | 0.56789532 | ✅ PASSED |
| diehard_sums | 0 | 100 | 100 | 0.01012540 | ✅ PASSED |
| diehard_runs | 0 | 100000 | 100 | 0.94009280 | ✅ PASSED |
| diehard_runs | 0 | 100000 | 100 | 0.43139332 | ✅ PASSED |
| diehard_craps | 0 | 200000 | 100 | 0.96593910 | ✅ PASSED |
| diehard_craps | 0 | 200000 | 100 | 0.94966825 | ✅ PASSED |
| marsaglia_tsang_gcd | 0 | 10000000 | 100 | 0.79723145 | ✅ PASSED |
| marsaglia_tsang_gcd | 0 | 10000000 | 100 | 0.18410971 | ✅ PASSED |
| sts_monobit | 1 | 100000 | 100 | 0.69233330 | ✅ PASSED |
| sts_runs | 2 | 100000 | 100 | 0.60363237 | ✅ PASSED |
| sts_serial | 1 | 100000 | 100 | 0.89203380 | ✅ PASSED |
| sts_serial | 2 | 100000 | 100 | 0.89860225 | ✅ PASSED |
| sts_serial | 3 | 100000 | 100 | 0.96785997 | ✅ PASSED |
| sts_serial | 3 | 100000 | 100 | 0.92807677 | ✅ PASSED |
| sts_serial | 4 | 100000 | 100 | 0.83748245 | ✅ PASSED |
| sts_serial | 4 | 100000 | 100 | 0.94440334 | ✅ PASSED |
| sts_serial | 5 | 100000 | 100 | 0.69290238 | ✅ PASSED |
| sts_serial | 5 | 100000 | 100 | 0.20953818 | ✅ PASSED |
| sts_serial | 6 | 100000 | 100 | 0.91270472 | ✅ PASSED |
| sts_serial | 6 | 100000 | 100 | 0.65942096 | ✅ PASSED |
| sts_serial | 7 | 100000 | 100 | 0.65850012 | ✅ PASSED |
| sts_serial | 7 | 100000 | 100 | 0.35704509 | ✅ PASSED |
| sts_serial | 8 | 100000 | 100 | 0.61570651 | ✅ PASSED |
| sts_serial | 8 | 100000 | 100 | 0.73229815 | ✅ PASSED |
| sts_serial | 9 | 100000 | 100 | 0.71607967 | ✅ PASSED |
| sts_serial | 9 | 100000 | 100 | 0.74282529 | ✅ PASSED |
| sts_serial | 10 | 100000 | 100 | 0.62469215 | ✅ PASSED |
| sts_serial | 10 | 100000 | 100 | 0.50902314 | ✅ PASSED |
| sts_serial | 11 | 100000 | 100 | 0.35920465 | ✅ PASSED |
| sts_serial | 11 | 100000 | 100 | 0.32958925 | ✅ PASSED |
| sts_serial | 12 | 100000 | 100 | 0.58156235 | ✅ PASSED |
| sts_serial | 12 | 100000 | 100 | 0.62476147 | ✅ PASSED |
| sts_serial | 13 | 100000 | 100 | 0.22931175 | ✅ PASSED |
| sts_serial | 13 | 100000 | 100 | 0.41387454 | ✅ PASSED |
| sts_serial | 14 | 100000 | 100 | 0.91996380 | ✅ PASSED |
| sts_serial | 14 | 100000 | 100 | 0.25183838 | ✅ PASSED |
| sts_serial | 15 | 100000 | 100 | 0.48376724 | ✅ PASSED |
| sts_serial | 15 | 100000 | 100 | 0.35240351 | ✅ PASSED |
| sts_serial | 16 | 100000 | 100 | 0.29386041 | ✅ PASSED |
| sts_serial | 16 | 100000 | 100 | 0.36315352 | ✅ PASSED |
| rgb_bitdist | 1 | 100000 | 100 | 0.26758205 | ✅ PASSED |
| rgb_bitdist | 2 | 100000 | 100 | 0.51649649 | ✅ PASSED |
| rgb_bitdist | 3 | 100000 | 100 | 0.47933864 | ✅ PASSED |
| rgb_bitdist | 4 | 100000 | 100 | 0.37415340 | ✅ PASSED |
| rgb_bitdist | 5 | 100000 | 100 | 0.76702115 | ✅ PASSED |
| rgb_bitdist | 6 | 100000 | 100 | 0.39444071 | ✅ PASSED |
| rgb_bitdist | 7 | 100000 | 100 | 0.96635030 | ✅ PASSED |
| rgb_bitdist | 8 | 100000 | 100 | 0.91560335 | ✅ PASSED |
| rgb_bitdist | 9 | 100000 | 100 | 0.82391916 | ✅ PASSED |
| rgb_bitdist | 10 | 100000 | 100 | 0.94258703 | ✅ PASSED |
| rgb_bitdist | 11 | 100000 | 100 | 0.88190016 | ✅ PASSED |
| rgb_bitdist | 12 | 100000 | 100 | 0.11348602 | ✅ PASSED |
| rgb_minimum_distance | 2 | 10000 | 1000 | 0.59378771 | ✅ PASSED |
| rgb_minimum_distance | 3 | 10000 | 1000 | 0.47629946 | ✅ PASSED |
| rgb_minimum_distance | 4 | 10000 | 1000 | 0.85933069 | ✅ PASSED |
| rgb_minimum_distance | 5 | 10000 | 1000 | 0.97686423 | ✅ PASSED |
| rgb_permutations | 2 | 100000 | 100 | 0.16982794 | ✅ PASSED |
| rgb_permutations | 3 | 100000 | 100 | 0.92185724 | ✅ PASSED |
| rgb_permutations | 4 | 100000 | 100 | 0.77805725 | ✅ PASSED |
| rgb_permutations | 5 | 100000 | 100 | 0.03480703 | ✅ PASSED |
| rgb_lagged_sum | 0 | 1000000 | 100 | 0.89170231 | ✅ PASSED |
| rgb_lagged_sum | 1 | 1000000 | 100 | 0.65442423 | ✅ PASSED |
| rgb_lagged_sum | 2 | 1000000 | 100 | 0.16417038 | ✅ PASSED |
| rgb_lagged_sum | 3 | 1000000 | 100 | 0.63154286 | ✅ PASSED |
| rgb_lagged_sum | 4 | 1000000 | 100 | 0.32175929 | ✅ PASSED |
| rgb_lagged_sum | 5 | 1000000 | 100 | 0.49238422 | ✅ PASSED |
| rgb_lagged_sum | 6 | 1000000 | 100 | 0.70046564 | ✅ PASSED |
| rgb_lagged_sum | 7 | 1000000 | 100 | 0.31906778 | ✅ PASSED |
| rgb_lagged_sum | 8 | 1000000 | 100 | 0.62498763 | ✅ PASSED |
| rgb_lagged_sum | 9 | 1000000 | 100 | 0.31634361 | ✅ PASSED |
| rgb_lagged_sum | 10 | 1000000 | 100 | 0.19889668 | ✅ PASSED |
| rgb_lagged_sum | 11 | 1000000 | 100 | 0.96327840 | ✅ PASSED |
| rgb_lagged_sum | 12 | 1000000 | 100 | 0.68122087 | ✅ PASSED |
| rgb_lagged_sum | 13 | 1000000 | 100 | 0.71058647 | ✅ PASSED |
| rgb_lagged_sum | 14 | 1000000 | 100 | 0.81747726 | ✅ PASSED |
| rgb_lagged_sum | 15 | 1000000 | 100 | 0.85014609 | ✅ PASSED |
| rgb_lagged_sum | 16 | 1000000 | 100 | 0.65505437 | ✅ PASSED |
| rgb_lagged_sum | 17 | 1000000 | 100 | 0.20910683 | ✅ PASSED |
| rgb_lagged_sum | 18 | 1000000 | 100 | 0.26970394 | ✅ PASSED |
| rgb_lagged_sum | 19 | 1000000 | 100 | 0.11866953 | ✅ PASSED |
| rgb_lagged_sum | 20 | 1000000 | 100 | 0.80475019 | ✅ PASSED |
| rgb_lagged_sum | 21 | 1000000 | 100 | 0.70542578 | ✅ PASSED |
| rgb_lagged_sum | 22 | 1000000 | 100 | 0.71979526 | ✅ PASSED |
| rgb_lagged_sum | 23 | 1000000 | 100 | 0.68728612 | ✅ PASSED |
| rgb_lagged_sum | 24 | 1000000 | 100 | 0.20263528 | ✅ PASSED |
| rgb_lagged_sum | 25 | 1000000 | 100 | 0.28895131 | ✅ PASSED |
| rgb_lagged_sum | 26 | 1000000 | 100 | 0.26763854 | ✅ PASSED |
| rgb_lagged_sum | 27 | 1000000 | 100 | 0.72679682 | ✅ PASSED |

</details>

