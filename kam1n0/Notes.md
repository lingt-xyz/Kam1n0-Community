## INDEXING

- File name was splitted by '.'
    - which means if there are two files named "abc.1" and "abc.2", asm2vec would process the first one and ignore the second one.
    
## BINARY COMPOSITION

- `Top-K` can be set to a smaller value, e.g. 2, to save matching time.
- `Threshold` is the threshold of the similarity score. It is __*not*__ the threshold of inline.
- `Avoid Same Binary` should be unchecked.
- Inline settings is the class `Asm2VecCloneDetectorIntegration`, line 189. 


## Testing

### Common settings

- Vector size: 200
- Top-K: 5
- Similarity Threshold: 0.6
- Uncheck `Avoid Same Binary`

### no-inline-10-random-walks

Settings:
- random walks: 10

Library:

- Train: libgmp-6.1.0-arm-clang-O0.so
- Test: libgmp-6.1.0-arm-clang-O3.so
    - Settings: 
        - Minimum block count: 1
    - Result:
        - did not find a match: 204
        - matched expected: 32
        - matched unexpected: 835
- Test: libgmp-6.1.0-arm-clang-O3.so
    - Settings: 
        - Minimum block count: 5
    - Result:
        - did not find a match: 31
        - matched expected: 24
        - matched unexpected: 365

### no-inline-1-random-walk

Settings:
- random walks: 1

Library:

- Train: libgmp-6.1.0-arm-clang-O0.so
- Test: libgmp-6.1.0-arm-clang-O3.so
    - Settings: 
        - Minimum block count: 1
    - Result:
        - did not find a match: 167
        - matched expected: 35
        - matched unexpected: 869
- Test: libgmp-6.1.0-arm-clang-O3.so
    - Settings: 
        - Minimum block count: 5
    - Result:
        - did not find a match: 45
        - matched expected: 29
        - matched unexpected: 346

### no-inline-1-random-walk

Settings:
- random walks: 1

Library:

- Train: 
    - libgmp-6.1.0-arm-clang-O0.so
    - libgmp-6.1.0-arm-gcc-O0.so
    - libMagickCore-7.Q16HDRI-7.0.1-10-arm-clang-O0.so.0.0.0
    - libssl-1.0.1f-arm-clang-O0.so
    - libssl-1.0.1f-arm-gcc-O0.so.1.0.0
    - libz-arm-clang-O0.so.1.2.7.1
    - vgpreload_core-arm-linux-arm-clang-O0.so
- Test: libgmp-6.1.0-arm-clang-O3.so
    - Settings: 
        - Minimum block count: 1
    - Result:
        - did not find a match: 155
        - matched expected: 54
        - matched unexpected: 862
- Test: libgmp-6.1.0-arm-clang-O3.so
    - Settings: 
        - Minimum block count: 5
    - Result:
        - did not find a match: 59
        - matched expected: 40
        - matched unexpected: 321