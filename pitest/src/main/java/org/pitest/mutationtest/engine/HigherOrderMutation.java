package org.pitest.mutationtest.engine;

import org.pitest.classinfo.ClassName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Can only represent mutants for one class
 */
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

    public ArrayList<MutationDetails> getAllMutationDetails() {
        return mutants;
    }

    public MutationDetails getMutationDetail(int i) {
        return mutants.get(i);
    }

    public List<MutationIdentifier> getIds() {
        return mutants.stream().map(md -> md.getId())
            .collect(Collectors.toList());
    }

    public List<Mutant> getMutants(Mutater mutater) {
        return getIds().stream().map(mutater::getMutation)
            .collect(Collectors.toList());
    }

    public ClassName getClassName() {
        if (mutants.size() > 0) {
            return mutants.get(0).getClassName();
        }
        return null;
    }

    public List<byte[]> getBytes(Mutater mutater) {
        return getMutants(mutater).stream().map(Mutant::getBytes)
            .collect(Collectors.toList());
    }

    @Override
    public HigherOrderMutation clone() {
        HigherOrderMutation newHOM = new HigherOrderMutation();
        for (MutationDetails id : getAllMutationDetails()) {
            newHOM.addMutation(id);
        }
        return newHOM;
    }

    public String toString() {
        return "HigherOrderMutation [order=" + getOrder() + ", FOMs=" + mutants.toString() + "]";
    }
}
