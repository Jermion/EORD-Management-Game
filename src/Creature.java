public class Creature {
    private BiologicalSystem biologicalSystem;
    private ArtificialSystem artificialSystem;

    public Creature() {
        biologicalSystem = new BiologicalSystem();
        artificialSystem = new ArtificialSystem();
    }

    public BiologicalSystem getBiologicalSystem() {
        return biologicalSystem;
    }

    public ArtificialSystem getArtificialSystem() {
        return artificialSystem;
    }
}
