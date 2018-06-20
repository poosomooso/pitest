package org.pitest.mutationtest.engine;

import org.pitest.classinfo.ClassName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    public Mutant getMutant(Mutater mutater) {
        return mutater.getManyMutations(getIds());
    }

    public ClassName getClassName() {
        if (mutants.size() > 0) {
            return mutants.get(0).getClassName();
        }
        return null;
    }

    public byte[] getBytes(Mutater mutater) {
        return getMutant(mutater).getBytes();
//        return getMutants(mutater).stream().map(Mutant::getBytes)
//            .collect(Collectors.toList());
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

    public static HigherOrderMutation fomAsHom(MutationDetails fom) {
        HigherOrderMutation m = new HigherOrderMutation();
        m.addMutation(fom);
        return m;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        HigherOrderMutation that = (HigherOrderMutation) o;
        Set<MutationDetails> myMutations = new HashSet<MutationDetails>(
            this.mutants);
        return myMutations.containsAll(that.mutants);
    }

    @Override
    public int hashCode() {
        return mutants != null ?
            mutants.stream().mapToInt(MutationDetails::hashCode).sum() : 0;
    }
}
