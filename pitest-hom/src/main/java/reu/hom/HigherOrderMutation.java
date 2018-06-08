package reu.hom;

import org.pitest.mutationtest.engine.MutationDetails;

import java.util.ArrayList;

public class HigherOrderMutation {
    private ArrayList<MutationDetails> mutants = new ArrayList<>();

    /**
     * TODO: Check if the new mutation is at the same location as an old one
     * @param mutationDetails
     */
    public void addMutation(MutationDetails mutationDetails) {
        if (mutants.contains(mutationDetails)) {
            throw new IllegalArgumentException(
                "This HOM already contains the mutation " + mutationDetails
                    .getId());
        }
        mutants.add(mutationDetails);
    }

    public int getOrder() {
        return mutants.size();
    }

    public ArrayList<MutationDetails> getMutants() {
        return mutants;
    }

    public MutationDetails getMutant(int i) {
        return mutants.get(i);
    }

    @Override
    public HigherOrderMutation clone() {
        HigherOrderMutation newHOM = new HigherOrderMutation();
        for (MutationDetails id : getMutants()) {
            newHOM.addMutation(id);
        }
        return newHOM;
    }

    public String toString() {
        return "HigherOrderMutation [order=" + getOrder() + ", FOMs=" + mutants.toString() + "]";
    }
}
