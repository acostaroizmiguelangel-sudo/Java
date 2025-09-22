public class INSERCCION {
    public static void insertionSort(int[] a) {
        for (int i = 1; i < a.length; i++) {
            int temp = a[i];
            int j = i - 1;
            while (j >= 0 && temp < a[j]) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = temp;
        }
    }

    public static void printArr(int[] a) {
        for (int value : a) {
            System.out.print(value + " ");
        }
    }

    public static void main(String[] args) {
        int[] a = {70, 15, 2, 51, 60};
        System.out.println("Esta es la lista antes de ordenarla: ");
        printArr(a);
        insertionSort(a);
        System.out.println("\nLa lista despues de ordenarla:");
        printArr(a);
    }
}

