package es.csic.iiia.maxsum.factors;

import es.csic.iiia.maxsum.Factor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@SuppressWarnings({"unchecked","rawtypes"})
public abstract class TwoSidedFactorTestAbstract extends CrossFactorTestAbstract {

    protected abstract AbstractTwoSidedFactor createFactor();

    @Test
    public void testAddA() {
        Factor[] neighbors = new Factor[]{mock(Factor.class), mock(Factor.class)};
        AbstractTwoSidedFactor<Factor> f = createFactor();

        f.addNeighbor(neighbors[0]);
        f.addNeighbor(neighbors[1]);
        f.setNElementsA(1);

        Factor factorA = mock(Factor.class);
        f.addANeighbor(factorA);

        assertEquals(3, f.getNeighbors().size());
        assertEquals(neighbors[0], f.getNeighbors().get(0));
        assertEquals(factorA, f.getNeighbors().get(1));
        assertEquals(neighbors[1], f.getNeighbors().get(2));
        assertEquals(2, f.getNElementsA());
    }

    @Test
    public void testAdd() {
        Factor[] neighbors = new Factor[]{mock(Factor.class), mock(Factor.class)};
        AbstractTwoSidedFactor<Factor> f = createFactor();
        f.setNElementsA(0);

        f.addANeighbor(neighbors[0]);
        f.addBNeighbor(neighbors[1]);

        Factor factorA = mock(Factor.class);
        f.addANeighbor(factorA);

        assertEquals(3, f.getNeighbors().size());
        assertEquals(neighbors[0], f.getNeighbors().get(0));
        assertEquals(factorA, f.getNeighbors().get(1));
        assertEquals(neighbors[1], f.getNeighbors().get(2));
        assertEquals(2, f.getNElementsA());
    }

    @Test
    public void testAddB() {
        Factor[] neighbors = new Factor[]{mock(Factor.class), mock(Factor.class)};
        AbstractTwoSidedFactor<Factor> f = createFactor();

        f.addNeighbor(neighbors[0]);
        f.addNeighbor(neighbors[1]);
        f.setNElementsA(1);

        Factor factorB = mock(Factor.class);
        f.addBNeighbor(factorB);

        assertEquals(3, f.getNeighbors().size());
        assertEquals(neighbors[0], f.getNeighbors().get(0));
        assertEquals(neighbors[1], f.getNeighbors().get(1));
        assertEquals(factorB, f.getNeighbors().get(2));
        assertEquals(1, f.getNElementsA());
    }

    @Test
    public void testRemoveA() {
        Factor[] neighbors = new Factor[]{mock(Factor.class), mock(Factor.class)};
        AbstractTwoSidedFactor<Factor> f = createFactor();

        f.addNeighbor(neighbors[0]);
        f.addNeighbor(neighbors[1]);
        f.setNElementsA(1);

        f.removeNeighbor(neighbors[0]);

        assertEquals(1, f.getNeighbors().size());
        assertEquals(neighbors[1], f.getNeighbors().get(0));
        assertEquals(0, f.getNElementsA());

        // Don't decrement nElementsA when removing a factor that is not a neighbor.
        f.removeNeighbor(neighbors[0]);

        assertEquals(1, f.getNeighbors().size());
        assertEquals(neighbors[1], f.getNeighbors().get(0));
        assertEquals(0, f.getNElementsA());
    }

    @Test
    public void testRemoveB() {
        Factor[] neighbors = new Factor[]{mock(Factor.class), mock(Factor.class)};
        AbstractTwoSidedFactor<Factor> f = createFactor();

        f.addNeighbor(neighbors[0]);
        f.addNeighbor(neighbors[1]);
        f.setNElementsA(1);

        f.removeNeighbor(neighbors[1]);

        assertEquals(1, f.getNeighbors().size());
        assertEquals(neighbors[0], f.getNeighbors().get(0));
        assertEquals(1, f.getNElementsA());

        // Don't decrement nElementsA when removing a factor that is not a neighbor.
        f.removeNeighbor(neighbors[1]);

        assertEquals(1, f.getNeighbors().size());
        assertEquals(neighbors[0], f.getNeighbors().get(0));
        assertEquals(1, f.getNElementsA());
    }
}
