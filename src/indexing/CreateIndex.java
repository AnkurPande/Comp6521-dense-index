/**
 * @author Ankurp 
 * 
 */

package indexing;

import input_output.IOFile;
import input_output.IndexWriter;
import input_output.WrongRecordOffsetSizeException;

import java.io.IOException;
import java.nio.ByteBuffer;

public class CreateIndex {
	
	// Constants for relation
	static final int BLOCK_SIZE = IOFile.BLOCK_SIZE;
	static final int RECORD_SIZE = IOFile.RECORD_SIZE;
	static final int AGE_OFFSET = IOFile.AGE_OFFSET;
	
	//static String path = "C:\\Users\\Ankurp\\DENSE-INDEX\\";
	static String path = "./resources/relation/";
	static String FILE_NAME = path+"person.txt";
	static int  NO_OF_TOUPLES = 10000;
	IndexWriter writer ;
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		new CreateIndex().runCases();
	}

	private void runCases() throws IOException{
		IOFile f = new IOFile(FILE_NAME);
		writer = new IndexWriter();
		//Calculate available memory at start.
		float startmem = Runtime.getRuntime().freeMemory();
		
		//start time
		long startTime = System.currentTimeMillis();
		
		//Calculate number of runs required
		int recordsPerBlock = BLOCK_SIZE / RECORD_SIZE;
		long fileSize = f.length();
		long recordsInRelation = fileSize / RECORD_SIZE;
		int runs = (int) Math.ceil((double)recordsInRelation/recordsPerBlock);
		
		byte[] tuple = new byte[2];
		ByteBuffer block = ByteBuffer.allocate(BLOCK_SIZE + RECORD_SIZE);
		
		long recordOffset = 0;
		for (int i = 0; i <= runs; i++) {
			block.put(f.readSequentialBlock());
			block.flip();
			while (block.remaining() >= RECORD_SIZE) {
				block.position(block.position() + AGE_OFFSET);
				// Read a tuple from block.
				block.get(tuple, 0, 2);
				
				//Converting values of age attributes and block sequence into 5 bytes offset.
				String age =  new String(tuple);
				short ageVal = Short.parseShort(age);

				//Add entry to index file.
				try {
					writer.addEntry(ageVal, recordOffset);
				} catch (WrongRecordOffsetSizeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				++recordOffset;
				block.position((block.position() / RECORD_SIZE) * RECORD_SIZE + RECORD_SIZE);
			}
			// Move remaining bytes to beginning of buffer
			block.compact();
		}
		
		writer.close();
		//Calculate the end time
		long endTime = System.currentTimeMillis();
		
		//Calculate the end memory.
		long endmem = Runtime.getRuntime().totalMemory();
		
		//Print performance.
		System.out.println("Time Taken : " + (endTime - startTime) + "ms");
		System.out.println("Memory Taken (in bytes): " + (endmem - startmem) + " bytes");
		System.out.println("Memory Taken (in MB): " + (double) (endmem - startmem) /(1024*1024) + " MB");
		System.out.println("Number of I/Os (creating index): " + f.getReads() + " reads, " + writer.getWrites() + " writes");
		}	
}
