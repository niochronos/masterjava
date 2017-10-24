package ru.javaops.masterjava.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {

        class MultiplyResult {
            final int colNumber;
            final int[] elements;

            public MultiplyResult(int colNumber, int[] elements) {
                this.colNumber = colNumber;
                this.elements = elements;
            }
        }

        final CompletionService<MultiplyResult> completionService = new ExecutorCompletionService<MultiplyResult>(executor);

        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        final List<Future<MultiplyResult>> futures = new ArrayList<>(matrixSize);

        for (int j = 0; j < matrixSize; j++) {
            final int col = j;
            futures.add(completionService.submit(new Callable<MultiplyResult>() {
                @Override
                public MultiplyResult call() throws Exception {
                    int elements[] = new int[matrixSize];

                    int thatColumn[] = new int[matrixSize];

                    for (int k = 0; k < matrixSize; k++) {
                        thatColumn[k] = matrixB[k][col];
                    }

                    for (int i = 0; i < matrixSize; i++) {
                        int thisRow[] = matrixA[i];
                        int sum = 0;
                        for (int k = 0; k < matrixSize; k++) {
                            sum += thisRow[k] * thatColumn[k];
                        }
                        elements[i] = sum;
                    }

                    return new MultiplyResult(col, elements);
                }
            }));
        }

        for (int i = 0; i < matrixSize; i++) {
            MultiplyResult multiplyResult = completionService.take().get();

            int col = multiplyResult.colNumber;
            int length = multiplyResult.elements.length;
            int elements[] = multiplyResult.elements;
            for (int j = 0; j < length; j++) {
                matrixC[j][col] = elements[j];
            }
        }

        return matrixC;
    }

    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        int thatColumn[] = new int[matrixSize];

        for (int j = 0; j < matrixSize; j++) {
            for (int k = 0; k < matrixSize; k++) {
                thatColumn[k] = matrixB[k][j];
            }

            for (int i = 0; i < matrixSize; i++) {
                int thisRow[] = matrixA[i];
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += thisRow[k] * thatColumn[k];
                }
                matrixC[i][j] = sum;
            }
        }

        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
