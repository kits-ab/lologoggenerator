package se.kits.stuff.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeightedOption {
    private String option;
    private double weight;

    public static String rollWeightedOptions(List<WeightedOption> weightedOptions) {
        double cumulative = 0.0;
        for (WeightedOption option : weightedOptions) {
            cumulative += option.getWeight();
        }
        double roll = Math.random() * cumulative;
        double stepping = 0.0;

        for (WeightedOption option : weightedOptions) {
            stepping += option.getWeight();
            if (stepping >= roll) {
                return option.getOption();
            }
        }
        throw new RuntimeException("Error at random selection. No valid option selected");
    }
}
