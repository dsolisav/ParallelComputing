package co.edu.unal.paralela;

import java.util.Random;

import junit.framework.TestCase;

public class ReciprocalArraySumTest extends TestCase {
    // Número de veces que se debe repetir cada prueba para dar consistencia de los resultados de en el tiempo.
    final static private int REPEATS = 60;

    private static int getNCores() {
            return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Crea un arreglo double[] de longitud N para utilizar como entrada para las pruebas.
     *
     * @param N Tamaño del arreglo a crear
     * @return Arreglo double de longitud N inicializado
     */
    private double[] createArray(final int N) {
        final double[] input = new double[N];
        final Random rand = new Random(314);

        for (int i = 0; i < N; i++) {
            input[i] = rand.nextInt(100);
            // No se permiten valores en cero en el arreglo de entrada para evitar la división por cero
            if (input[i] == 0.0) {
                i--;
            }
        }

        return input;
    }

    /**
     * Una implementación de referencia de seqArraysum, en caso de que alguno del archvo del código fuente principal sea modificado accidentalmente.
     *
     * @param input Enrada para calcular secuencialmente la suma de los recíprocos
     * @return Suma de los recíprocos de la entrada
     */
    private double seqArraySum(final double[] input) {
        double sum = 0;

        // Calcula la suma de los recíprocos de los elementos del arreglo
        for (int i = 0; i < input.length; i++) {
            sum += 1 / input[i];
        }

        return sum;
    }

    /**
     * Una función 'helper' para hacer las pruebas de la implementación de dos y la implementación de muchas tareas en paralelo.
     *
     * @param N Tamaño del arreglo utilizado para las pruebas
     * @param useManyTaskVersion Switch entre el código la versión de dos tareas en paralelo y la versión de muchas tareas en paralelo
     * @param ntasks Número de tareas a utilizar
     * @return La mejora en la rapidez (speedup) alcanzada, no todas las pruebas utilizan esta información
     */
    private double parTestHelper(final int N, final boolean useManyTaskVersion, final int ntasks) {
        // Crea un arreglo de entrada de manera aleatoria
        final double[] input = createArray(N);
        // Utilza una version secuencial para calcular el resultado correcto
        final double correct = seqArraySum(input);
        // Utiliza la implementación paralela para calcular el resultado
        double sum;
        if (useManyTaskVersion) {
            sum = ReciprocalArraySum.parManyTaskArraySum(input, ntasks);
        } else {
            assert ntasks == 2;
            sum = ReciprocalArraySum.parArraySum(input);
        }
        final double err = Math.abs(sum - correct);
        // Asegura que la salida esperada sea la calculada
        final String errMsg = String.format("No concuerda el resultado para N = %d, valor esperado = %f, valor calculado = %f, error " +
                "absoluto = %f", N, correct, sum, err);
        assertTrue(errMsg, err < 1E-2);

        /*
         * Ejecuta varias repeticiones de la versiones secuncial y paralela para obtener una medida más exacta del desempeño paralelo.
         */
        final long seqStartTime = System.currentTimeMillis();
        for (int r = 0; r < REPEATS; r++) {
            seqArraySum(input);
        }
        final long seqEndTime = System.currentTimeMillis();

        final long parStartTime = System.currentTimeMillis();
        for (int r = 0; r < REPEATS; r++) {
            if (useManyTaskVersion) {
                ReciprocalArraySum.parManyTaskArraySum(input, ntasks);
            } else {
                assert ntasks == 2;
                ReciprocalArraySum.parArraySum(input);
            }
        }
        final long parEndTime = System.currentTimeMillis();

        final long seqTime = (seqEndTime - seqStartTime) / REPEATS;
        final long parTime = (parEndTime - parStartTime) / REPEATS;

        return (double)seqTime / (double)parTime;
    }

    /**
     * Prueba que la implementación de dos tareas en paralelo calcula correctamente los resultados para arreglos con un millón de elementos.
     */
    public void testParSimpleTwoMillion() {
        final double minimalExpectedSpeedup = 1.5;
        final double speedup = parTestHelper(2_000_000, false, 2);
        final String errMsg = String.format("Se esperaba que la implementación de dos tareas en paralelo pudiera ejecutarse " +
                " %fx veces más rápido, pero solo alcanzo a mejorar la rapidez (speedup) %fx veces", minimalExpectedSpeedup, speedup);
        assertTrue(errMsg, speedup >= minimalExpectedSpeedup);
    }

    /**
     * Prueba que la implementación de dos tareas en paralelo calcula correctamente los resultados para arreglos con cientos de millones de elementos..
     */
    public void testParSimpleTwoHundredMillion() {
        final double speedup = parTestHelper(200_000_000, false, 2);
        final double minimalExpectedSpeedup = 1.5;
        final String errMsg = String.format("Se esperaba que la implementación de dos tareas en paralelo pudiera ejecutarse " +
                "%fx veces más rápido, pero solo alcanzo a mejorar la rapidez (speedup) %fx veces", minimalExpectedSpeedup, speedup);
        assertTrue(errMsg, speedup >= minimalExpectedSpeedup);
    }

    /**
     * Prueba que la implementación de muchas tareas en paralelo calcula correctamente los resultados para arreglos con un millónde elementos.
     */
    public void testParManyTaskTwoMillion() {
        final int ncores = getNCores();
        final double minimalExpectedSpeedup = (double)ncores * 0.6;
        final double speedup = parTestHelper(2_000_000, true, ncores);
        final String errMsg = String.format("Se esperaba que la implmentación de muchas tareas en paralelo pudiera ejecutarse " +
                "%fx veces más rápido, pero solo alcanzo a mejorar la rapidez (speedup) %fx veces", minimalExpectedSpeedup, speedup);
        assertTrue(errMsg, speedup >= minimalExpectedSpeedup);
    }

    /**
     * Prueba que la implementación de muchas tareas en paralelo calcula correctamente los resultados para arreglos con cientos de millones de elementos.
     */
    public void testParManyTaskTwoHundredMillion() {
        final int ncores = getNCores();
        final double speedup = parTestHelper(200_000_000, true, ncores);
        final double minimalExpectedSpeedup = (double)ncores * 0.8;
        final String errMsg = String.format("Se esperaba que la implmentación de muchas tareas en paralelo pudiera ejecutarse " +
                " %fx veces más rápido, pero solo alcanzo a mejorar la rapidez (speedup) %fx veces", minimalExpectedSpeedup, speedup);
        assertTrue(errMsg, speedup >= minimalExpectedSpeedup);
    }
}
