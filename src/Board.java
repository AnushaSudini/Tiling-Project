

public class Board extends Piece {

	char[][] square;
	int[][] squareId;
	private int size;
	private int squareNotFilled = 0;
	int height;
	int width;

	public Board(int id, RawPiece raw) {
		this.id = id;
		this.size = raw.size();
		this.squareNotFilled = size;

		int rawLeft = raw.getLeft();
		int rawTop = raw.getTop();
		this.right = raw.getRight() - rawLeft;
		this.bottom = raw.getBottom() - rawTop;

		height = bottom+1;
		width = right+1;
		square = new char[height][width];
		squareId = new int[height][width];
		for (int j = square.length; j-- > 0;) {
			for (int i = square[j].length; i-- > 0;) {
				square[j][i] = Piece.BLANK;
				squareId[j][i] = -1;
			}
		}
		for (Square p : raw.points) {
			square[p.y - rawTop][p.x - rawLeft] = p.ch;//put the values of the board making the board corrdinated from 0
			//so except for holes all the squres will be havinh some value
			squareId[p.y - rawTop][p.x - rawLeft] = id;
		}
	}

	public long addtime = 0;
	public long removetime = 0;

	public boolean placeTile(Tile tile, int row, int col) {
		int height = height();
		int width = width();
		int[] pX = tile.pX[tile.curO];//gets the coordinates from the current orientation being used on board
		int[] pY = tile.pY[tile.curO];
		char[] pCh = tile.pCh[tile.curO];
		int x, y;
		for (int index = tile.size(); index-- > 0;) {
			x = col + pX[index];//check if it should be px[index-1] becoz it is an array and starts from 0. test it once
			y = row + pY[index];
			if (y < 0 || y >= height || x < 0 || x >= width//check if the tile can fit on the board without coming outside
					|| square[y][x] != pCh[index] || squareId[y][x] != id) // implicitly
				return false;
		}
		for (int index = tile.size(); index-- > 0;) {
			x = col + pX[index];
			y = row + pY[index];

			squareId[y][x] = tile.id;
		}
		squareNotFilled -= tile.size();
		return true;
	}
	
	public void removeTile(Tile tile, int row, int col) {
		int[] pX = tile.pX[tile.curO];
		int[] pY = tile.pY[tile.curO];
		int x, y;
		for (int index = tile.size(); index-- > 0;) {
			x = col + pX[index];
			y = row + pY[index];

			squareId[y][x] = id;
		}
		squareNotFilled += tile.size();
	}

	//This method just takes the values present in each square and creates n new merged board
	public MergedBoard getMergedBoard() {

		char[][] data = new char[height][width];
		for (int j = height; j-- > 0;)
			for (int i = width; i-- > 0;)
				data[j][i] = square[j][i];

		for (int j = height; j-- > 0;)
			for (int i = width; i-- > 0;)
				if (squareId[j][i] != id)
					data[j][i] = Piece.BLANK;

		return new MergedBoard(data, false);
	}

	public Solution getSolution() {
		return new Solution(this, getData());
	}

	public int size() {
		return size;
	}

	public boolean isFull() {
		return squareNotFilled == 0;
	}

	public IntChPair[][] getData() {
		IntChPair[][] data = new IntChPair[height][width];
		for (int j = square.length; j-- > 0;)
			for (int i = square[j].length; i-- > 0;)
				data[j][i] = new IntChPair(squareId[j][i], square[j][i]);
		return data;
	}

	@Override
	public int width() {
		
		return right+1;
	}

	@Override
	public int height() {
		return bottom+1;
		
	}
}
