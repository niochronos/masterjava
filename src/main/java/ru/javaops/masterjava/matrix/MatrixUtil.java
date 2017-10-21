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

    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final CompletionService<List<MultiplyResult>> completionService = new ExecutorCompletionService<List<MultiplyResult>>(executor);

        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        final List<Future<List<MultiplyResult>>> futures = new ArrayList<>(matrixSize);

        for (int col = 0; col < matrixSize; col++) {
            futures.add(completionService.submit(new MultiplyMatrixTask(matrixA, matrixB, col, matrixSize)));
        }

        while (!futures.isEmpty()) {
            Future<List<MultiplyResult>> listFuture = completionService.poll();
            if (listFuture != null) {
                List<MultiplyResult> multiplyResultList = listFuture.get();
                futures.remove(listFuture);

                for (MultiplyResult multiplyResult : multiplyResultList) {
                    matrixC[multiplyResult.row][multiplyResult.col] = multiplyResult.sum;
                }
            }
        }

        return matrixC;
    }

    // TODO optimize by https://habrahabr.ru/post/114797/
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

    private static class MultiplyResult {
        final int row;
        final int col;
        final int sum;

        public MultiplyResult(int row, int col, int sum) {
            this.row = row;
            this.col = col;
            this.sum = sum;
        }
    }

    private static class MultiplyMatrixTask implements Callable<List<MultiplyResult>> {
        final int[][] matrixA;
        final int[][] matrixB;
        final int col;
        final int matrixSize;

        public MultiplyMatrixTask(int[][] matrixA, int[][] matrixB, int col, int matrixSize) {
            this.matrixA = matrixA;
            this.matrixB = matrixB;
            this.col = col;
            this.matrixSize = matrixSize;
        }

        @Override
        public List<MultiplyResult> call() throws Exception {
            final List<MultiplyResult> list = new ArrayList<>(matrixSize);

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
                list.add(new MultiplyResult(i, col, sum));
            }

            return list;
        }
    }
}
