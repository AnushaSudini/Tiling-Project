

public class Solution extends Piece {

	public IntChPair[][] data;
	public int size;

	protected Solution(Board board, IntChPair[][] data) {
		this.right = board.right;
		this.bottom = board.bottom;

		this.data = data;
		this.size = board.size();
	}
	
	@Override
	public IntChPair[][] getData() {
		return data;
	}
	
	public boolean sameAs(Solution ref) {
		int height = ref.bottom+1;
		int width = ref.right+1;

		int[] oArr;
		if (height == width) {
			oArr = new int[] { ROTATE_0, ROTATE_90, ROTATE_180, ROTATE_270,
					FLIP_ROTATE_0, FLIP_ROTATE_90, FLIP_ROTATE_180,
					FLIP_ROTATE_270 };
		} else {
			oArr = new int[] { ROTATE_0, ROTATE_180, FLIP_ROTATE_0,
					FLIP_ROTATE_180 };
		}

		boolean matched = false;
		int i, j;
		for (int index = 0; index < oArr.length && !matched; index++) {
			for (j = 0; j < height && !matched; j++) {
				for (i = 0; i < width && !matched; i++) {
					if (!ref.data[j][i]
							.equals(data[getY(i, j, oArr[index])][getX(i, j,
									oArr[index])])) {
						break;
					}
				}
				if (i < width)
					break;
			}
			matched = j == height;
		}
		return matched;
	}

	@Override//try removing the below 3 methods from this class and from piece class
	public int width() {
		// TODO Auto-generated method stub
		return 0 ;
	}

	@Override
	public int height() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

}
