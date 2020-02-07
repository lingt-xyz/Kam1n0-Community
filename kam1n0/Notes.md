## INDEXING

- File name was splitted by '.'
    - which means there are two files "abc.1" and "abc.2", asm2vec would process the first one and ignore the second one.
    
## BINARY COMPOSITION

- `Top-K` can be set to a smaller value, e.g. 2, to save matching time.
- `Threshold` is the threshold of the similarity score. It is __*not*__ the threshold of inline.
- `Avoid Same Binary` should be unchecked.
- Inline settings is the class `Asm2VecCloneDetectorIntegration`, line 189. 
