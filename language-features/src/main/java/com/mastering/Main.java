void main() {
    record Point(int x, int y) {
    }

    IO.println("Hello and welcome!");
    for (int i = 1; i <= 5; i++) {
        IO.println("i = " + i);
    }

    var point = new Point(1, 2);
    IO.println(point);
}
