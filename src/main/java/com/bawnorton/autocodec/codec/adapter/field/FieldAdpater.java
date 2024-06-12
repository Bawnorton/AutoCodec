package com.bawnorton.autocodec.codec.adapter.field;

import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.StatementNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;
import java.util.Collection;

/**
 * Field adapters handle how the generated constructor handles field types<br>
 * For example, the {@link NormalFieldAdpater} maps the fields to parameters of the same type and assigns them simply:<br>
 * <pre>
 *     {@code
 *     private Field field;
 *
 *     public ClassType(Field field) {
 *         this.field = field;
 *     }
 *     }
 * </pre>
 * On the other hand the {@link ListFieldAdpater} maps the fields to {@code List<T>} parameters and assigns the field through a
 * copy-ctor or a no-arg ctor followed by a {@link List#addAll(Collection)} call depending on what is avaliable:
 * <pre>
 *     {@code
 *     private IntArrayList intList;
 *
 *     public ClassType(List<Integer> intList) {
 *          this.intList = new IntArrayList();
 *          this.intList.addAll(intList);
 *     }
 * </pre>
 */
public abstract class FieldAdpater extends ContextHolder {
    public FieldAdpater(ProcessingContext context) {
        super(context);
    }

    public abstract Type getParameterType(Type fieldType);

    public abstract VariableDeclNode getParameter(VariableDeclNode field);

    public abstract List<StatementNode> createAssignmentStatements(VariableDeclNode field);
}
