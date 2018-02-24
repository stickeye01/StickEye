import java.util.Scanner;

public class JOO {

	public static void main(String[] args) {
	
		
		double norm, norm1, norm2;
		Scanner scan= new Scanner(System.in);
		double num1, num2,num3,num;
		System.out.println("오른쪽 거리 입력하시오");
		num1=scan.nextDouble();
		System.out.println("중앙 거리 연락하시오");
		num2=scan.nextDouble();
		System.out.println("왼쪽 거리 입력하시오");
		num3=scan.nextDouble();
		/*
		 * 오른쪽 거리가 더 길면은 
		 * 벡터의 합을 이용해야하므로
		 * 
		 */
		if(num1>num3)
		{
			num=num1;
			norm1=Math.sqrt(Math.pow(num*Math.cos(45), 2)+Math.pow((num*Math.sin(45)+num2) ,2));// v1 두 벡터의 크기
			norm2=Math.sqrt(Math.pow(num*Math.cos(45), 2)+Math.pow(num*Math.sin(45),2)); //v2 벡터의 크기
			norm=norm1*norm2; // v1 * v2
			double prod=((num*Math.cos(45))*num*Math.cos(45))+((num*Math.sin(45)+num2)*num*Math.sin(45)); //두 벡터의 내적
			double angle=Math.acos(prod/norm); 
		
			System.out.println( 45+angle/3.141592654*180); 
		}
		else if(num1<num3)
		{
			num=num3;
			norm1=Math.sqrt(Math.pow(num*Math.cos(45), 2)+Math.pow((num*Math.sin(45)+num2) ,2));// v1 두 벡터의 크기
			norm2=Math.sqrt(Math.pow(num*Math.cos(45), 2)+Math.pow(num*Math.sin(45),2)); //v2 벡터의 크기
			norm=norm1*norm2; // v1 * v2
			double prod=((num*Math.cos(45))*num*Math.cos(45))+((num*Math.sin(45)+num2)*num*Math.sin(45)); //두 벡터의 내적
			double angle=Math.acos(prod/norm); 

			System.out.println( 180-(45+angle/3.141592654*180)); 

		}
		else if(num1==num3)
		{
			System.out.println(90);
		}

	}

}
