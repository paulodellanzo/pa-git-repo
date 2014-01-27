/**
 * 
 */
package fiuba.pyp;

/**
 * @author pyp
 *
 */
public class IT_Transformation extends Transformation {

	/**
	 *  (see) Page 27 Direccionamiento de Datos & OT FAQ <br>
	 *
	 *	Conditions: op2 has already been executed, op1 has not 
	 *  and its parameters are from the state before op2 was executed
	 *  op1 is executed as if op2 was executed so its included
	 *  
	 */
	@Override
	public Operation transform(Operation op1, Operation op2) {
		// TODO Auto-generated method stub
		if (op1.getType().equals("INSERT") && op2.getType().equals("INSERT")){
			return IT_InsertInsert(op1, op2);
		}
		else if (op1.getType().equals("INSERT") && op2.getType().equals("DELETE")){
			return IT_InsertDelete(op1, op2);
		}
		else if (op1.getType().equals("DELETE") && op2.getType().equals("INSERT")){
			return IT_DeleteInsert(op1, op2);
		}
		else if (op1.getType().equals("DELETE") && op2.getType().equals("DELETE")){
			return IT_DeleteDelete(op1, op2);
		}
		return null;
	}

	public Operation IT_InsertInsert(Operation op1, Operation op2) {
		if (op1.getPosition() < op2.getPosition())
			return op1;
		else{
			Operation opAux = op1;
			opAux.setPosition(op1.getPosition() + op2.getObj().getLength());
			return opAux;
		}
	}
	
	public Operation IT_InsertDelete(Operation op1, Operation op2) {
		if (op1.getPosition() <= op2.getPosition())
			return op1;
		else{
			Operation opAux = op1;
			opAux.setPosition(op1.getPosition() - op2.getObj().getLength());
			return opAux;
		}
		/* String-wise
		else if (op1.getPosition() > op2.getPosition() + op2.getObj().getLength()){
			Operation opAux = op1;
			opAux.setPosition(op1.getPosition() - op2.getObj().getLength());
			return opAux;
		}
		else{
			Operation opAux = op1;
			opAux.setPosition(op2.getPosition());
			return opAux;
		}*/
	}
	
	public Operation IT_DeleteInsert(Operation op1, Operation op2) {
		if (op1.getPosition() < op2.getPosition())
			return op1;
		else{
			Operation opAux = op1;
			opAux.setPosition(op1.getPosition() + op2.getObj().getLength());
			return opAux;
		}	
		/*	String-Wise
		 * 
		 * if (op2.getPosition() >= op1.getPosition() + op1.getObj().getLength())
			return op1;
		else if (op1.getPosition() >= op2.getPosition()){
			Operation opAux = op1;
			opAux.setPosition(op1.getPosition() + op2.getObj().getLength());
			return opAux;
		}
		else{
			return op1;
		}*/
	}
	
	public Operation IT_DeleteDelete(Operation op1, Operation op2) {
		if (op1.getPosition() < op2.getPosition())
			return op1;
		else if (op1.getPosition() > op2.getPosition()){
			Operation opAux = op1;
			opAux.setPosition(op1.getPosition() - op2.getObj().getLength());
			return opAux;
		}
		else{
			return null;
		}
		/*	String-Wise
		 * 
		if (op2.getPosition() >= op1.getPosition() + op1.getObj().getLength())
			return op1;
		else if (op1.getPosition() >= op2.getPosition() + op2.getObj().getLength()){
			//Delete (L(op1, P(op1) - L(op1))
			Operation opAux = op1;
			opAux.setPosition(op1.getPosition() - op2.getObj().getLength());
			return opAux;
		}
		else{
			if (op2.getPosition() <= op1.getPosition() && (op1.getPosition() + op1.getObj().getLength() <= op2.getPosition() + op2.getObj().getLength()))
				//Delete (0, L(op1))
				return null;
			else if (op2.getPosition() <= op1.getPosition() && (op1.getPosition() + op1.getObj().getLength() > op2.getPosition() + op2.getObj().getLength())){
				//Delete (P(op1) + L(op1) - (P(op2) + L(op2)), P(op2))
				Operation opAux = op1;
				opAux.setPosition(op2.getPosition());
				return opAux;
			}
			else if (op2.getPosition() > op1.getPosition() && (op1.getPosition() + op1.getObj().getLength() <= op2.getPosition() + op2.getObj().getLength())){
				//Delete (P(op2) - P(op1), P(op1))
				return op1;
			}
			else{
				//Delete (L(op1) - L(op2), P(op1))
				return op1;
			}
		}*/
	}
	
}