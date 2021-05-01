//Txt based database
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileOutputStream;

public class TXTDataBase {        
	String splitMark = ";";

	public void writeStringToFile(String path,String text){
		try{
			byte []contents = text.getBytes();
			File aimFile = new File(path);
			FileOutputStream out = new FileOutputStream(aimFile);		
			out.write(contents);
			out.close();
		}
		catch (Exception e){	
			e.printStackTrace();
		}
	}

	public String getStringFromFile(String path){
		String string = "";
		try{
			BufferedReader br = new BufferedReader(new FileReader(path));
			String temp = "";
			while((temp = br.readLine()) != null) {
				if(temp.trim().equals("")){
					continue;
				}
				System.out.println("temp" + temp);
				string += temp + "\r\n";
			}
			br.close();
		}
		catch (Exception e){	
			e.printStackTrace();
		}
		return string;
	}

	public String getFirstLineNotNullFromFile(String path){
		String string = "";
		try{
			BufferedReader br = new BufferedReader(new FileReader(path));
			String temp = "";
			while((temp = br.readLine()) != null) {
				if(temp.trim().equals("")){
					continue;
				}
				string = temp;
				break;
			}
			br.close();
		}
		catch (Exception e){	
			e.printStackTrace();
		}
		return string;
	}

	public Map<String,String> getConfigFromFile(String path){
		Map<String, String> params = new HashMap<String, String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(path));
			String temp = "";
			while((temp = br.readLine()) != null) {
				if(temp.trim().equals("") || temp.contains("//") || !temp.contains("=")){
					continue;
				}
				String temps[] = temp.split("=");
				params.put(temps[0].trim(),temps[1].trim());
			}
			br.close();
		}
		catch (Exception e){	
			e.printStackTrace();
		}
		return params;
	}

	public String isExsitAndGetItem(String path,String[] texts,int[] positions){
		try{
			if(texts.length < 1 || texts.length != positions.length){
				System.out.println("input data error");
				return "";
			}

			BufferedReader br = new BufferedReader(new FileReader(path));
			String temp = null;
			
			while((temp = br.readLine()) != null) {
				String columns[] = temp.split(splitMark);
				for(int i = 0 ; i < columns.length ; i++){
					columns[i] = columns[i].trim();
				}
				boolean isFind = true;
				for(int i = 0 ; i < texts.length ; i++){
					if(positions[i] - 1 < 0 || positions[i] - 1 >= columns.length){
						System.out.println("IndexError");
						return "";
					}
					if(!texts[i].equals(columns[positions[i] - 1])){
						isFind = false;
						break;
					}
				}
				if(isFind){
					return temp;
				}
			}
			br.close();
		}
		catch (Exception e){	
			e.printStackTrace();
		}
		return "";
	}

	public String insert(String path,String text,int keyPosition){
		try{
			keyPosition -= 1;
			File file = new File(path);
			BufferedReader br = new BufferedReader(new FileReader(file));

			String values[] = text.split(splitMark);
			String temp = null;
			while((temp = br.readLine()) != null) {
				String columns[] = temp.split(splitMark);
				for(int i = 0 ; i < columns.length ; i++){
					columns[i] = columns[i].trim();
				}
				if(keyPosition < 0 || keyPosition >= columns.length || keyPosition >= values.length){
					return "IndexError";
				}
				if(columns[keyPosition].equals(values[keyPosition])){
					return "repeat";
				}
			}
			byte []contents = (text + "\r\n").getBytes();
		
			FileOutputStream out = new FileOutputStream(file,true);		//add
			out.write(contents);
			out.close();
		}
		catch (Exception e){	
			e.printStackTrace();
			return "error";
		}
		System.out.println("write ok");
		return "success";
	}

	public String update(String path,String key,int keyPosition,String value,int valuePosition){
		valuePosition -=1;
		keyPosition -= 1;
		try{
			BufferedReader br = new BufferedReader(new FileReader(path));
			String temp = null;
			String record = "";
			
			while((temp = br.readLine()) != null) {
				String columns[] = temp.split(splitMark);
				for(int i = 0 ; i < columns.length ; i++){
					columns[i] = columns[i].trim();
				}
				if(keyPosition < 0 || valuePosition < 0 || keyPosition >= columns.length || valuePosition >= columns.length){
					return "IndexError";
				}
				if(key.equals("")){
					return "keyEmpty";
				}
				if(key.equals(columns[keyPosition])){
					String t = "";
					for(int j = 0;j < columns.length;j++){
						if(j!= valuePosition){
							t += columns[j] + ";";
						}
						else{
							t += value + ";";
						}
					}
					record += t.substring(0,t.length()-1) + "\r\n";
				}
				else{
					record += temp + "\r\n";
				}
			}
			br.close();
			File txt = new File(path);					
			FileOutputStream out = new FileOutputStream(txt);	
			byte []contents = (record).getBytes();
			out.write(contents);
			out.close();
			System.out.println("update ok");
		}
		catch (Exception e){	
			e.printStackTrace();
			return "error";
		}
		return "success";
	}

	public String updateAsAddNumber(String path,String key,int keyPosition,int changeValue,int valuePosition){
		valuePosition -=1;
		keyPosition -= 1;
		try{
			BufferedReader br = new BufferedReader(new FileReader(path));
			String temp = null;
			String record = "";
			
			while((temp = br.readLine()) != null) {
				String columns[] = temp.split(splitMark);
				for(int i = 0 ; i < columns.length ; i++){
					columns[i] = columns[i].trim();
				}
				if(keyPosition < 0 || valuePosition < 0 || keyPosition >= columns.length || valuePosition >= columns.length){
					return "IndexError";
				}
				if(key.equals("")){
					return "keyEmpty";
				}
				if(key.equals(columns[keyPosition])){
					String t = "";
					for(int j = 0;j < columns.length;j++){
						if(j!= valuePosition){
							t += columns[j] + ";";
						}
						else{
							t += (Integer.parseInt(columns[j]) + changeValue) + ";";
						}
					}
					record += t.substring(0,t.length()-1) + "\r\n";
				}
				else{
					record += temp + "\r\n";
				}
			}
			br.close();
			File txt = new File(path);					
			FileOutputStream out = new FileOutputStream(txt);	//overwrite
			byte []contents = (record).getBytes();
			out.write(contents);
			out.close();
			System.out.println("update ok");
		}
		catch (Exception e){	
			e.printStackTrace();
			return "error";
		}
		return "success";
	}

	public String delete(String path,String key,int keyPosition){
		keyPosition -= 1;
		try{
			BufferedReader br = new BufferedReader(new FileReader(path));
			String temp = null;
			String record = "";
			
			while((temp = br.readLine()) != null) {
				String columns[] = temp.split(splitMark);
				for(int i = 0 ; i < columns.length ; i++){
					columns[i] = columns[i].trim();
				}
				if(keyPosition < 0 || keyPosition >= columns.length){
					return "IndexError";
				}
				if(key.equals("")){
					return "keyEmpty";
				}
				if(key.equals(columns[keyPosition])){
					continue;
				}
				record += temp + "\r\n";
			}
			br.close();
			File txt = new File(path);					
			FileOutputStream out = new FileOutputStream(txt);	
			byte []contents = (record).getBytes();
			out.write(contents);
			out.close();
			System.out.println("delete ok");
		}
		catch (Exception e){	
			e.printStackTrace();
			return "error";
		}
		return "success";
	}

	public String getItem(String path,String key,int keyPosition){
		keyPosition -= 1;
		String results = "";
		try{
			File file = new File(path);
			BufferedReader br = new BufferedReader(new FileReader(file));

			String temp = null;
			while((temp = br.readLine()) != null) {
				String columns[] = temp.split(splitMark);
				for(int i = 0 ; i < columns.length ; i++){
					columns[i] = columns[i].trim();
				}
				if(keyPosition < 0 || keyPosition >= columns.length){
					return "IndexError";
				}
				if(key.equals("")){
					return "keyEmpty";
				}
				if(key.equals(columns[keyPosition])){
					return temp;
				}
			}
		}
		catch (Exception e){	
			e.printStackTrace();
		}
		return "";
	}

	public String getValuesNotKey(String path,String key,int keyPosition,int aimPosition){
		keyPosition -= 1;
		aimPosition -= 1;
		String results = "";
		try{
			File file = new File(path);
			BufferedReader br = new BufferedReader(new FileReader(file));

			String temp = null;
			while((temp = br.readLine()) != null) {
				String columns[] = temp.split(splitMark);
				for(int i = 0 ; i < columns.length ; i++){
					columns[i] = columns[i].trim();
				}
				if(keyPosition < 0 || aimPosition < 0 || keyPosition >= columns.length || aimPosition >= columns.length){
					return "IndexError";
				}
				if(key.equals("")){
					return "keyEmpty";
				}
				if(key.equals(columns[keyPosition])){
					results += columns[aimPosition] + ";";
				}
			}
		}
		catch (Exception e){	
			e.printStackTrace();
		}
		return results;
	}

	public ArrayList<String> getItemsNotKey(String path,String key,int keyPosition){
		ArrayList<String> result = new ArrayList<String>();
		keyPosition -= 1;
		try{
			File file = new File(path);
			BufferedReader br = new BufferedReader(new FileReader(file));

			String temp = null;
			while((temp = br.readLine()) != null) {
				String columns[] = temp.split(splitMark);
				for(int i = 0 ; i < columns.length ; i++){
					columns[i] = columns[i].trim();
				}
				if(keyPosition < 0 || keyPosition >= columns.length){
					System.out.println("IndexError");
					return null;
				}
				if(key.equals("")){
					System.out.println("keyEmpty");
					return null;
				}
				if(key.equals(columns[keyPosition])){
					result.add(temp);
				}
			}
		}
		catch (Exception e){	
			e.printStackTrace();
		}
		return result;
	}

	public String deleteWithoutKey(String path,String[] texts,String[] text2s,String[] splitMarks){
		try{
			String text = "";
			String text2 = "";
			if(texts.length > 0){
				for(int i = 0;i < texts.length;i++){
					text += texts[i];
					if(i < splitMarks.length){
						text += splitMarks[i];
					}
				}
			}
			if(text2s.length > 0){
				for(int i = 0;i < text2s.length;i++){
					text2 += text2s[i];
					if(i < splitMarks.length){
						text2 += splitMarks[i];
					}
				}
			}
			BufferedReader br = new BufferedReader(new FileReader(path));
			String temp = null;
			String record = "";
			System.out.println(text);
			System.out.println(text2);
			
			while((temp = br.readLine()) != null) {
				if(!text.equals("")){
					if(temp.equals(text)){
						continue;
					}
				}
				if(!text2.equals("")){
					if(temp.equals(text2)){
						continue;
					}
				}
				record += temp + "\r\n";
			}
			br.close();
			File txt = new File(path);					
			FileOutputStream out = new FileOutputStream(txt);	
			byte []contents = (record).getBytes();
			out.write(contents);
			out.close();
			System.out.println("delete ok");
		}
		catch (Exception e){
			e.printStackTrace();
			return "error";
		}
		return "success";
	}

	public boolean isExsitWithoutKey(String path,String[] texts,String[] splitMarks){
		try{
			String text = "";
			if(texts.length > 0){
				for(int i = 0;i < texts.length;i++){
					text += texts[i];
					if(i < splitMarks.length){
						text += splitMarks[i];
					}
				}
			}
			BufferedReader br = new BufferedReader(new FileReader(path));
			String temp = null;
			String record = "";
			System.out.println(text);
			
			while((temp = br.readLine()) != null) {
				if(!text.equals("")){
					if(temp.equals(text)){
						return true;
					}
				}
			}
			br.close();
		}
		catch (Exception e){	
			e.printStackTrace();
		}
		return false;
	}

	public boolean addWithoutKey(String path,String[] texts,String[] text2s,String[] splitMarks){
		try{
			String text = "";
			String text2 = "";
			if(texts.length > 0){
				for(int i = 0;i < texts.length;i++){
					text += texts[i];
					if(i < splitMarks.length){
						text += splitMarks[i];
					}
				}
			}
			if(text2s.length > 0){
				for(int i = 0;i < text2s.length;i++){
					text2 += text2s[i];
					if(i < splitMarks.length){
						text2 += splitMarks[i];
					}
				}
			}
			BufferedReader br = new BufferedReader(new FileReader(path));
			String temp = null;
			System.out.println(text);
			System.out.println(text2);

			String record = "";
			if(!text.equals("")){
				record += text + "\r\n";
			}
			if(!text2.equals("")){
				record += text2 + "\r\n";
			}
			
			File txt = new File(path);							
			FileOutputStream out = new FileOutputStream(txt,true);		//add
			byte []contents = (record).getBytes();
			out.write(contents);
			out.close();
			System.out.println("add ok");
		}
		catch (Exception e){	
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
