
	import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public class BoardFilling {

		private Board board;
		private Tile[] tiles;

		private boolean allSameSizeTiles;
		private int singleTileSize;
		private int totalTileSquare;

		private Tile[] participants;

		private HashSet<IMonitor> monitors = new HashSet<IMonitor>();

		public void addMonitor(IMonitor monitor) {
			if (monitor != null)
				monitors.add(monitor);
		}

		public void removeMonitor(IMonitor monitor) {
			if (monitor != null)
				monitors.remove(monitor);
		}

		protected void OnProgress(int event) {
			for (IMonitor monitor : monitors) {
				monitor.update(event);
			}
		}

		public Tile[] getTiles() {
			return tiles;
		}

		public Board getBoard() {
			return board;
		}

		public ArrayList<Solution> getSolutions() {
			return sols;
		}

		public int getSolutionCount() {
			return sols.size();
		}

	/*	public void parse(String path) throws IOException {
			parse(new FileInputStream(new File(path)));
		}*/

		//public void parse(InputStream inStream) throws IOException {
		public void parse(String fileName) throws IOException {
			ArrayList<char[]> fullInput = new ArrayList<char[]>();
			
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			//BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
			try {
				String line = null;
				while ((line = in.readLine()) != null) {
					fullInput.add(line.toCharArray());
				}
			} finally {
				in.close();
			}

			MergedBoard merged = new MergedBoard(fullInput, false);
			merged.parse();
			ArrayList<RawPiece> rawPieces = merged.getRawPieces(); // total number of points in the input file
			rawPieces.remove(merged.getMaxPiece());
			board = new Board(0, merged.getMaxPiece());
			//System.out.println("printing board");
			board.print();
			
			tiles = new Tile[rawPieces.size()];

			singleTileSize = rawPieces.get(0).size();
			allSameSizeTiles = true;
			int i = 0;
			totalTileSquare = 0;
			for (RawPiece rawPiece : rawPieces) {
				tiles[i] = new Tile(i + 1, rawPiece);
				tiles[i].print();
				totalTileSquare += tiles[i].size();
				if (singleTileSize != tiles[i].size())
					allSameSizeTiles = false;
				i++;
			}
		}

		private int[] tilesStatus;
		private int[] orientation =  new int[] { Piece.ROTATE_0, Piece.ROTATE_90,
				Piece.ROTATE_180, Piece.ROTATE_270, Piece.FLIP_ROTATE_0,
				Piece.FLIP_ROTATE_90, Piece.FLIP_ROTATE_180,
				Piece.FLIP_ROTATE_270 };//check if this is removed..not req as here we will be using all rotation and ref

		public ArrayList<Solution> sols = new ArrayList<Solution>();
		private boolean singleSolution;

		long allSolTime = 0;
		long firstSolTime =0;
		long deadEndTime = 0;
		long startTime = 0;
		//check if singleSolution, boolean useRotation, use reflection are of any use, if no delete them
		public void solve() {
			//sols.clear();//remove
			allSolTime = 0;
			//this.singleSolution = singleSolution;//remove

			//setOrientationMode(useRotation, useReflection); can be removed
			for (int index = 0; index < tiles.length; index++) {
				tiles[index].process(orientation);//this is the place where all tiles positions are calculated for all orientations
			}

			if (board.size() == totalTileSquare) {

				participants = new Tile[tiles.length];
				for (int index = 0; index < tiles.length; index++) {
					participants[index] = tiles[index];
				}
				solveThis();
			} else if (board.size() < totalTileSquare) {//check this later

				selectParticipants(new BitSet(tiles.length), 0, board.size());
			} else {

				allSolTime = 0;
			}
		}

		private void selectParticipants(BitSet pbits, int index, int unfilled) {
			int newUnFilled;
			for (int i = 2; i-- > 0;) {
				if (i == 1) {
					pbits.set(index);
					newUnFilled = unfilled - tiles[index].size();
				} else {
					pbits.clear(index);
					newUnFilled = unfilled;
				}

				if (newUnFilled > 0) {
					if (index + 1 < tiles.length)
						selectParticipants(pbits, index + 1, newUnFilled);
				} else if (newUnFilled == 0) {
					participants = new Tile[pbits.cardinality()];
					int k = 0;
					for (int j = 0; j <= index; j++) {
						if (pbits.get(j))
							participants[k++] = tiles[j];
					}
					solveThis();
				} else
					return;

				if (singleSolution && sols.size() == 1) {
					return;
				}
			}
		}

		private void solveThis() {

			//System.out.println("in solve this fn");
			int startRow = 0;
			int startCol = 0;
			
			tilesStatus = new int[participants.length];//it keeps track of which tile is placed on the board
			for (int i = 0; i < participants.length; i++)
				tilesStatus[i] = i;
			
			//check if this is req or not. because this is called only once i.e when the board is empty
			while (board.squareId[startRow][startCol] != board.id) {
				startCol++;
				if (startCol == board.width()) {
					startCol = 0;
					startRow++;
				}
			}

			//check these three variables
			this.deadEndTime = 0;
			this.board.addtime = 0;
			this.board.removetime = 0;
			startTime = System.currentTimeMillis();//check if u can call in different way

			play(startRow, startCol);

			allSolTime = System.currentTimeMillis() - startTime;
			//System.err.println(String.format(
					//"addtime = %s ms removetime = %s ms, deadEndTime = %s ms",
					//board.addtime, board.removetime, this.deadEndTime));
		}
//this places the tiles on the board and is done recursively
		private void play(int row, int col) {
			
			//System.out.println("in play");
			for (int index = 0; index < participants.length; index++) {
				if (tilesStatus[index] < 0)
					continue; // tile already used

				Tile tile = participants[tilesStatus[index]];
				for (tile.curO = 0; tile.curO < tile.oCount; tile.curO++) {
					if (!board.placeTile(tile, row, col))
						continue;
					tilesStatus[index] -= tilesStatus.length;

					if (board.isFull()) {
						Solution sol = board.getSolution();
						 if (!foundSymmetry(sol)) 
							 sols.add(sol);
						if(sols.size() == 1){
							firstSolTime = System.currentTimeMillis() - startTime;
						}
						//OnProgress(IMonitor.NEW_SOL);
						// }
					} else if (!cannotProceedForward()) {
						// find next empty space, going
						// left-to-right then top-to-bottom
						int nextRow = row;
						int nextCol = col;
						while (board.squareId[nextRow][nextCol] != board.id) {
							nextCol++;
							if (nextCol == board.width()) {
								nextCol = 0;
								nextRow++;
							}
						}
						play(nextRow, nextCol);
					}

					board.removeTile(tile, row, col);
					tilesStatus[index] += tilesStatus.length;
				}
			}
		}

		public boolean cannotProceedForward() {
			MergedBoard merged = board.getMergedBoard();
			merged.parse();//I feel somewhere it's going wrong
			if (allSameSizeTiles) {//check if allSameSizeTiles value will ever be true
				ArrayList<RawPiece> rawPieces = merged.getRawPieces();
				for (RawPiece raw : rawPieces) {
					if (raw.size() % singleTileSize != 0)
						return true;
				}
				return false;
			} else {
				int size;

				// min sized holes
				int minHoleSize = merged.getMinPiece().size();
				int minHoleCount = 0;
				// max sized holes
				int maxHoleSize = merged.getMaxPiece().size();
				int maxHoleCount = 0;
				for (RawPiece raw : merged.getRawPieces()) {
					size = raw.size();
					if (size == minHoleSize)
						minHoleCount++;
					if (size == maxHoleSize)
						maxHoleCount++;
				}
				int minTileSize = Integer.MAX_VALUE;
				int maxTileSize = Integer.MIN_VALUE;

				for (int i = 0; i < participants.length; i++) {
					if (tilesStatus[i] < 0)
						continue;
					size = participants[tilesStatus[i]].size();
					if (size < minTileSize)
						minTileSize = size;
					if (size == minHoleSize)
						minHoleCount--;

					if (size > maxTileSize)
						maxTileSize = size;
					if (size == maxHoleSize)
						maxHoleCount--;
				}
				return minTileSize > minHoleSize
						|| (minTileSize == minHoleSize && minHoleCount > 0)
						|| maxTileSize > maxHoleSize
						|| (maxTileSize == maxHoleSize && maxHoleCount < 0);
			}
		}

		public void print() {
			for (Tile tile : participants) {
				tile.print();
				System.out.println();
			}
			board.print();
			System.out.println();
			printSolutions();
		}

		public void printSolutions() {
			System.out.println("# of Solutions: " + sols.size());
			int count = 0;
			for (Solution sol : sols) {
				System.out.println("Solution #: " + count++);
				sol.print();
				System.out.println();
			}
		}

		public void printLastSolution() {
			int index = sols.size() - 1;
			if (index >= 0) {
				Solution sol = sols.get(index);
				System.out.println("Solution #: " + index);
				sol.print();
				System.out.println();
			}
		}

		public void removeSymmetric() {
			long startTime = System.currentTimeMillis();
			int index, ref;
			for (index = 0; index < sols.size();) {
				for (ref = 0; ref < index;) {
					if (sols.get(index).sameAs(sols.get(ref))) {
						sols.remove(index);
						break;
					} else
						ref++;
				}
				if (ref == index)
					index++;
			}
			allSolTime += (System.currentTimeMillis() - startTime);
		}

		public boolean foundSymmetry(Solution s) {
			for (int ref = 0; ref < sols.size();) {
				if (s.sameAs(sols.get(ref))) {
					return true;
				} else
					ref++;
			}
			return false;
		}
	}
