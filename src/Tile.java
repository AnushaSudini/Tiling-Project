

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Tile extends Piece {
	private RawPiece raw;
	private int size;

	int[][] pX;
	int[][] pY;
	char[][] pCh;
	int[][] tDim; // 0 = height, 1 = width
	int oCount;
	int curO;

	public Tile(int id, RawPiece raw) {
		this.id = id;
		this.raw = raw;
		this.size = raw.size();
		process(new int[] { Piece.ROTATE_0 });
	}
//this is called from solve method(with all types of orientations and the above method
	public void process(int[] orientation) {
		int rawLeft = raw.getLeft();
		int rawTop = raw.getTop();
		this.right = raw.getRight() - rawLeft;//It gets the right cordination of the tile withrespect from the 00 cordinates
		this.bottom = raw.getBottom() - rawTop;

		int o;
		ArrayList<Square[]> lstPoints = new ArrayList<Square[]>();
		ArrayList<int[]> lstDimention = new ArrayList<int[]>();
		oCount = 0;
		int adjX;
		int index;
		Square[] points;
		Square[] ref;
		boolean matched;
		int x, y;
		for (int oi = 0; oi < orientation.length; oi++) {
			o = orientation[oi];

			adjX = Integer.MAX_VALUE;
			for (Square sq : raw.points) {
				x = sq.x - rawLeft; // making all points in the raw piece from 00...in the raw piece the values are taken with respect to file position
				y = sq.y - rawTop;

				if (getY(x, y, o) == 0) {
					if (getX(x, y, o) <= adjX) {
						adjX = getX(x, y, o);//for 0 orientation the value of adjx would be the right most value of x
						
					}
				}
			}
			index = 0;
			points = new Square[this.size];
			for (Square sq : raw.points) {
				x = sq.x - rawLeft;
				y = sq.y - rawTop;

				points[index++] = new Square(getX(x, y, o) - adjX,// this is the new  positions of points of tile for a particular rotation
						getY(x, y, o), sq.ch);//for the above statement for 0 orientation the values of x starts from -adjx to 0 and same y values
			}
			//sort(T[] a, Comparator<? super T> c):Sorts the specified array of objects according to the order induced by the specified comparator
			Arrays.sort(points, new Comparator<Square>() {
				
				public int compare(Square o1, Square o2) {
					return (o1.y - o2.y) != 0 ? o1.y - o2.y : o1.x - o2.x;
				}
			});

			// if new, add to lstPoints
			//this is used to prevent repetetions, suppose if both flip and rotation has same coordinates then, it is not inserted into the 1stpoints. hence it sufficient to check only 2 orientations of the tile
			matched = false;
			for (curO = oCount; curO-- > 0 && !matched;) {
				ref = lstPoints.get(curO);//i feel that ref holds the square array that contains points for one particular orientation
				for (index = size; index-- > 0;) {
					if (!ref[index].equals(points[index])) {
						break;
					}
				}
				matched = index == -1;//this will be true only of all the elements in the ref and points match at least for one orientation
			}
			//did not understand
			if (!matched) {
				lstPoints.add(points);
				if ((o & RF_90_270) == 0)
					lstDimention.add(new int[] { bottom + 1, right + 1 });
				else
					lstDimention.add(new int[] { right + 1, bottom + 1 });
				oCount++;
			}
		}
		pX = new int[oCount][size];//orientation order will be according to how the orientation params are passed
		pY = new int[oCount][size];
		pCh = new char[oCount][size];
		tDim = new int[oCount][];
		//after getting points for all orientations, add different points for orientations are stored in pX,pY
		for (curO = 0; curO < lstPoints.size(); curO++) {
			ref = lstPoints.get(curO);
			for (index = 0; index < size; index++) {
				pX[curO][index] = ref[index].x;
				pY[curO][index] = ref[index].y;
				pCh[curO][index] = ref[index].ch;
			}
			tDim[curO] = lstDimention.get(curO);
		}
		curO = 0;
	}

	@Override
	public int height() {
		return tDim[curO][0];
	}

	@Override
	public int width() {
		return tDim[curO][1];
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public IntChPair[][] getData() {
		int adjX = Integer.MAX_VALUE;
		for (int i = pX[curO].length; i-- > 0;) {
			if (pX[curO][i] < adjX)
				adjX = pX[curO][i];
		}
		IntChPair[][] data = new IntChPair[height()][width()];
		for (int j = data.length; j-- > 0;)
			for (int i = data[j].length; i-- > 0;)
				data[j][i] = new IntChPair(id, Piece.BLANK);

		for (int index = pX[curO].length; index-- > 0;) {
			data[pY[curO][index]][pX[curO][index] - adjX]
					.setB(pCh[curO][index]);
		}
		return data;
	}

	@Override
	public void print() {
		IntChPair[][] data = getData();
		System.out.println("id = " + id);
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				System.out.print(data[i][j]);
			}
			System.out.println();
		}
	}

	public void printAll() {
		StringBuilder sb = new StringBuilder();
		sb.append("id = ");
		sb.append(id);
		sb.append(", oCount = ");
		sb.append(oCount);
		sb.append(NEWLINE);
		for (int oi = 0; oi < oCount; oi++) {
			sb.append("{");
			for (int index = 0; index < size; index++) {
				sb.append(", ");
				sb.append(pY[oi][index]);
				sb.append(", ");
				sb.append(pX[oi][index]);
			}
			sb.append("}");
			sb.append(NEWLINE);
		}
		System.out.println(sb.toString());
	}
}
