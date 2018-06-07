package reu.hom;

import org.pitest.mutationtest.engine.Mutant;

import java.util.ArrayList;

public class HigherOrderMutation {
    private ArrayList<Mutant> mutants = new ArrayList<>();

    public void addMutation(Mutant id) {
        mutants.add(id);
    }

    public int getOrder() {
        return mutants.size();
    }

    public ArrayList<Mutant> getMutants() {
        return mutants;
    }

    public Mutant getMutant(int i) {
        return mutants.get(i);
    }

    @Override
    public HigherOrderMutation clone() {
        HigherOrderMutation newHOM = new HigherOrderMutation();
        for (Mutant id : getMutants()) {
            newHOM.addMutation(id);
        }
        return newHOM;
    }

    public String toString() {
        return "HigherOrderMutation [order=" + getOrder() + ", FOMs=" + mutants.toString() + "]";
    }
}
