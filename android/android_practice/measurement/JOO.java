import java.util.Scanner;

public class JOO {

	public static void main(String[] args) {
	
		
		double norm, norm1, norm2;
		Scanner scan= new Scanner(System.in);
		double num1, num2,num3,num;
		System.out.println("������ �Ÿ� �Է��Ͻÿ�");
		num1=scan.nextDouble();
		System.out.println("�߾� �Ÿ� �����Ͻÿ�");
		num2=scan.nextDouble();
		System.out.println("���� �Ÿ� �Է��Ͻÿ�");
		num3=scan.nextDouble();
		/*
		 * ������ �Ÿ��� �� ����� 
		 * ������ ���� �̿��ؾ��ϹǷ�
		 * 
		 */
		if(num1>num3)
		{
			num=num1;
			norm1=Math.sqrt(Math.pow(num*Math.cos(45), 2)+Math.pow((num*Math.sin(45)+num2) ,2));// v1 �� ������ ũ��
			norm2=Math.sqrt(Math.pow(num*Math.cos(45), 2)+Math.pow(num*Math.sin(45),2)); //v2 ������ ũ��
			norm=norm1*norm2; // v1 * v2
			double prod=((num*Math.cos(45))*num*Math.cos(45))+((num*Math.sin(45)+num2)*num*Math.sin(45)); //�� ������ ����
			double angle=Math.acos(prod/norm); 
		
			System.out.println( 45+angle/3.141592654*180); 
		}
		else if(num1<num3)
		{
			num=num3;
			norm1=Math.sqrt(Math.pow(num*Math.cos(45), 2)+Math.pow((num*Math.sin(45)+num2) ,2));// v1 �� ������ ũ��
			norm2=Math.sqrt(Math.pow(num*Math.cos(45), 2)+Math.pow(num*Math.sin(45),2)); //v2 ������ ũ��
			norm=norm1*norm2; // v1 * v2
			double prod=((num*Math.cos(45))*num*Math.cos(45))+((num*Math.sin(45)+num2)*num*Math.sin(45)); //�� ������ ����
			double angle=Math.acos(prod/norm); 

			System.out.println( 180-(45+angle/3.141592654*180)); 

		}
		else if(num1==num3)
		{
			System.out.println(90);
		}

	}

}
