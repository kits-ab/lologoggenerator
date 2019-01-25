package se.kits.stuff.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeightedOption<T> {
    private T option;
    private double weight;
}
